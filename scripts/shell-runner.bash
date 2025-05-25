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

# 🔥 4️⃣ Start shell-runner in foreground with JVM system properties (including disabling web server!)
java \
  -Dspring.datasource.url="$DB_URL" \
  -Dspring.datasource.username="$DB_USERNAME" \
  -Dspring.datasource.password="$DB_PASSWORD" \
  -Dspring.datasource.driver-class-name="$DB_DRIVER" \
  -Dspring.jpa.database-platform="$DB_DIALECT" \
  -Dspring.main.web-application-type=none \
  -jar shell-runner.jar

echo "✅ shell-runner finished. Logs: $LOG_FILE"
