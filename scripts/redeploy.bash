#!/usr/bin/env bash
# redeploy.bash — requires environment variables (no defaults)
# Exits if any required env var is missing.
set -euo pipefail

# --- Required environment variables (no defaults!) ---
# REQUIRED:
#   REMOTE_HOST   - remote host (e.g. idaia)
#   REMOTE_PORT   - ssh port (e.g. 22)
#   REMOTE_USER   - remote user (e.g. davido)
#   REMOTE_DIR    - remote directory where app lives (e.g. /opt/cyberstrak)
#   LOCAL_JAR     - absolute path to local jar that should be uploaded
#   JAR_NAME      - target jar file name on remote (e.g. rest-runner.jar)
# Optional:
#   SSH_KEY       - path to private key (optional; if set, will be used with -i)
#   SSH_OPTIONS   - additional SSH options (optional, e.g. "-oBatchMode=yes")

required_vars=(REMOTE_HOST REMOTE_PORT REMOTE_USER REMOTE_DIR LOCAL_JAR JAR_NAME)

missing=()
for v in "${required_vars[@]}"; do
  if [[ -z "${!v:-}" ]]; then
    missing+=("$v")
  fi
done

if (( ${#missing[@]} )); then
  echo "ERROR: missing required environment variables: ${missing[*]}" >&2
  echo "Please export them before running this script. Aborting." >&2
  exit 2
fi

# Build SSH base command carefully (don't print secrets)
SSH_BASE=(ssh -p "${REMOTE_PORT}" -o ConnectTimeout=10)
if [[ -n "${SSH_KEY:-}" ]]; then
  SSH_BASE+=(-i "${SSH_KEY}")
fi
if [[ -n "${SSH_OPTIONS:-}" ]]; then
  # allow caller to append secure options; do NOT add insecure defaults here
  # e.g. SSH_OPTIONS="-oBatchMode=yes"
  # Note: caller is responsible for providing safe options
  # shellcheck disable=SC2086
  SSH_BASE+=(${SSH_OPTIONS})
fi

SCP_CMD=(scp -P "${REMOTE_PORT}")
if [[ -n "${SSH_KEY:-}" ]]; then
  SCP_CMD+=(-i "${SSH_KEY}")
fi

# Sanity checks for LOCAL_JAR
if [[ ! -f "${LOCAL_JAR}" ]]; then
  echo "ERROR: LOCAL_JAR does not exist or is not a file: ${LOCAL_JAR}" >&2
  exit 3
fi

echo "Uploading ${LOCAL_JAR} to ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/${JAR_NAME} ..."
"${SCP_CMD[@]}" "${LOCAL_JAR}" "${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR}/${JAR_NAME}"

echo "Upload finished. Restarting remote service..."

# Remote commands — executed on remote host. Assume remote rest-runner script is in REMOTE_DIR
# IMPORTANT: This sequence expects that:
#  - a) remote has a script to start the app (rest-runner.bash) WITHOUT internal nohup,
#  - b) we will start it via nohup on the remote via this outer script.
#
# We avoid exposing env values in logs. We only print high-level status.

remote_cmds=$(cat <<'ENDCMD'
set -euo pipefail
cd "$REMOTE_DIR"
# stop existing process if running (attempt graceful stop by pkill or using pid file)
# adjust this block to match how your app should be stopped.
if pgrep -f rest-runner.jar >/dev/null 2>&1; then
  echo "Stopping existing process..."
  pkill -f rest-runner.jar || true
  sleep 2
fi

# cleanup old logs (optional)
if [[ -d logs ]]; then
  # rotate or keep, here we just remove old rotated file if you want:
  # rm -f logs/cyberstrak.log.1 || true
  :
fi

# Start the app in background using nohup from this outer process.
echo "Starting new instance (nohup) ..."
nohup ./rest-runner.bash >/dev/null 2>&1 &
echo "STARTED"
ENDCMD
)

# Run remote commands. Pass REMOTE_DIR as env var to remote shell to avoid exposing other sensitive inline values.
# We avoid printing secrets and do not embed secrets into the remote_cmds here.
"${SSH_BASE[@]}" "${REMOTE_USER}@${REMOTE_HOST}" "REMOTE_DIR='${REMOTE_DIR}' bash -s" <<EOF
${remote_cmds}
EOF

echo "Deployment done."

