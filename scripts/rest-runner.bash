#!/bin/bash

# 🚀 Usage:
# ./rest-runner.bash jdbc:mysql://localhost:3306/mydb myuser mypass
# Or:
# export DB_URL=jdbc:mysql://localhost:3306/mydb
# export DB_USERNAME=myuser
# export DB_PASSWORD=mypass
# export DB_DRIVER=org.mariadb.jdbc.Driver
# ./rest-runner.bash

# 1️⃣ Read arguments OR environment variables
DB_URL="${1:-$DB_URL}"
DB_USERNAME="${2:-$DB_USERNAME}"
DB_PASSWORD="${3:-$DB_PASSWORD}"
DB_DRIVER="${4:-$DB_DRIVER}"
ISSUER_ID="${5:-$ISSUER_ID}"
ISSUER_SECRET="${6:-$ISSUER_SECRET}"
LOG_FILE="${7:-$LOG_FILE}"

# 🛑 2️⃣ Validate
if [ -z "$DB_URL" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ] || [ -z "$DB_DRIVER" ] || [ -z "$ISSUER_ID" ] || [ -z "$ISSUER_SECRET" ] || [ -z "$LOG_FILE" ]; then
  echo "Usage: ./rest-runner.bash <DB_URL> <DB_USERNAME> <DB_PASSWORD> <DRIVER_CLASS> <ISSUER_ID> <ISSUER_SECRET> <LOG_FILE>"
  echo "Or set the environment variables: DB_URL, DB_USERNAME, DB_PASSWORD, DB_DRIVER ISSUER_ID ISSUER_SECRET LOG_FILE"
  exit 1
fi

# 🔥 3️⃣ Auto-detect Hibernate dialect
if [[ "$DB_URL" == jdbc:mysql:* ]] || [[ "$DB_URL" == jdbc:mariadb:* ]]; then
  DB_DIALECT="org.hibernate.dialect.MariaDBDialect"
elif [[ "$DB_URL" == jdbc:postgresql:* ]]; then
  DB_DIALECT="org.hibernate.dialect.PostgreSQLDialect"
elif [[ "$DB_URL" == jdbc:h2:* ]]; then
  DB_DIALECT="org.hibernate.dialect.H2Dialect"
elif [[ "$DB_URL" == jdbc:sqlite:* ]]; then
  DB_DIALECT="org.hibernate.dialect.SQLiteDialect"
else
  echo "❌ Could not auto-detect dialect for JDBC URL: $DB_URL"
  echo "Please set it manually with: export DB_DIALECT=org.hibernate.dialect.YourDialect"
  exit 1
fi

# 🔥 5️⃣ Start app with dynamic DB config
nohup java \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Dspring.datasource.driver-class-name="$DB_DRIVER" \
  -Dspring.jpa.database-platform="$DB_DIALECT" \
  -Dissuer.id="$ISSUER_ID" \
  -Dissuer.secret="$ISSUER_SECRET" \
  -Dlogging.file.name="$LOG_FILE" \
  -jar rest-runner.jar \
  &

echo "✅ rest-runner started in background. Logs: $LOG_FILE"
