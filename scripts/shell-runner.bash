#!/bin/bash

# 🚀 Usage:
# ./shell-runner.bash <DB_URL> <DB_USERNAME> <DB_PASSWORD> <DRIVER_CLASS>
# Or:
# export DB_URL=jdbc:postgresql://localhost:5432/mydb
# export DB_USERNAME=myuser
# export DB_PASSWORD=mypass
# export DB_DRIVER=org.postgresql.Driver
# ./shell-runner.bash

# 1️⃣ Read arguments OR environment variables
DB_URL="${1:-$DB_URL}"
DB_USERNAME="${2:-$DB_USERNAME}"
DB_PASSWORD="${3:-$DB_PASSWORD}"
DB_DRIVER="${4:-$DB_DRIVER}"

# 🛑 2️⃣ Validate
if [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ] || [ -z "$DB_DRIVER" ]; then
  echo "Usage: ./shell-runner.bash <DB_URL> <DB_USERNAME> <DB_PASSWORD> <DRIVER_CLASS>"
  echo "Or set the environment variables: DB_URL, DB_USERNAME, DB_PASSWORD, DB_DRIVER"
  exit 1
fi

# 🔥 3️⃣ Log file
LOG_FILE="shell-runner.log"

# 🔥 4️⃣ Start shell-runner in foreground, log to file
java -jar shell-runner.jar \
  --spring.datasource.url="$DB_URL" \
  --spring.datasource.username="$DB_USERNAME" \
  --spring.datasource.password="$DB_PASSWORD" \
  --spring.datasource.driver-class-name="$DB_DRIVER" \
  > "$LOG_FILE" 2>&1

echo "✅ shell-runner finished. Logs: $LOG_FILE"
