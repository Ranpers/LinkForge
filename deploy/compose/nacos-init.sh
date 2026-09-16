#!/bin/sh
set -eu

if [ "${NACOS_USERNAME}" != "nacos" ]; then
  echo "NACOS_USERNAME must be 'nacos' because the Nacos admin initialization API creates that fixed account" >&2
  exit 1
fi

login() {
  curl --fail --silent --show-error --request POST \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}" \
    http://nacos:8848/nacos/v3/auth/user/login >/dev/null
}

if login; then
  exit 0
fi

curl --fail --silent --show-error --request POST \
  --data-urlencode "password=${NACOS_PASSWORD}" \
  http://nacos:8848/nacos/v3/auth/user/admin >/dev/null

login
