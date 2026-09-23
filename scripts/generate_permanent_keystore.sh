#!/usr/bin/env bash
# ==============================================================================
# SHΞN™ tunnel ᴢᴇʀᴏ - Permanent Keystore Generator for Google Android Standards
# Based on Google signature standards (RSA 2048, 10,000 days validity, v1/v2/v3/v4)
# ==============================================================================
set -e

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RESET='\033[0m'

echo -e "${CYAN}================================================================${RESET}"
echo -e "${CYAN}       SHΞN™ tunnel ᴢᴇʀᴏ - Permanent Keystore Generator         ${RESET}"
echo -e "${CYAN}================================================================${RESET}"

STORE_PASS=$(head -c 32 /dev/urandom | base64 | tr -dc A-Za-z0-9 | head -c 16)
ALIAS_NAME="shen_release_$(date +%s)"
KEYSTORE_FILE="shen-release.jks"

echo -e "${YELLOW}>> Generating permanent Google-compliant RSA 2048-bit certificate (10,000 days)...${RESET}"

# Generate private key and certificate conforming to Google Play and Android OS standards
openssl req -x509 -newkey rsa:2048 -nodes -days 10000 \
  -keyout key.pem -out cert.pem \
  -subj "/CN=SHEN-Tunnel-Zero/OU=Security/O=SHEN-Global/C=US" 2>/dev/null

# Package into PKCS12 / JKS format
openssl pkcs12 -export -out "$KEYSTORE_FILE" \
  -inkey key.pem -in cert.pem \
  -name "$ALIAS_NAME" \
  -password "pass:$STORE_PASS" 2>/dev/null

rm -f key.pem cert.pem

BASE64_KEY=$(base64 -w 0 "$KEYSTORE_FILE")

echo -e "${GREEN}✓ Keystore generated successfully: $KEYSTORE_FILE${RESET}"
echo ""
echo -e "${CYAN}----------------------------------------------------------------${RESET}"
echo -e "${CYAN}  ADD THESE SECRETS TO GITHUB REPOSITORY (Settings -> Secrets)  ${RESET}"
echo -e "${CYAN}----------------------------------------------------------------${RESET}"
echo -e "KEYSTORE_PASSWORD : ${GREEN}$STORE_PASS${RESET}"
echo -e "KEY_ALIAS         : ${GREEN}$ALIAS_NAME${RESET}"
echo -e "KEY_PASSWORD      : ${GREEN}$STORE_PASS${RESET}"
echo -e "KEYSTORE_BASE64   : (Base64 string below)"
echo -e "${YELLOW}$BASE64_KEY${RESET}"
echo -e "${CYAN}----------------------------------------------------------------${RESET}"
echo -e "Keep this keystore safe! Using these secrets guarantees that every future build"
echo -e "has the EXACT SAME digital signature and can be installed over previous builds."
echo -e "☬ Exclusive SHΞN™ made"
