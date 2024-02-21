#!/bin/bash

# This script is used to generate traffic to the API server.
# It creates, retrieves, searches, and deletes 100 product items.
# It expects the API server to be running on localhost:8080.
# API server host and port can be overridden by setting the HOST and PORT environment variables.

HOST=${HOST:-localhost}
PORT=${PORT:-8080}
BASE_URL="http://$HOST:$PORT/api"

PRODUCT_NAMES=("Alpha" "Beta" "Gamma" "Delta" "Epsilon")
PRODUCT_DESCRIPTIONS=("High Quality" "Eco Friendly" "Innovative Design" "Budget Friendly" "Top Performance")

get_random_element() {
    local array=("$@")
    echo "${array[RANDOM % ${#array[@]}]}"
}

test_api() {
    PRODUCT_NAME=$(get_random_element "${PRODUCT_NAMES[@]}")
    PRODUCT_DESCRIPTION=$(get_random_element "${PRODUCT_DESCRIPTIONS[@]}")

    CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/product" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$PRODUCT_NAME\",
            \"description\": \"$PRODUCT_DESCRIPTION\",
            \"price\": 100.00
        }")

    PRODUCT_ID=$(echo $CREATE_RESPONSE | jq '.id')
    echo -e "Product created with ID: $PRODUCT_ID\n"

    if [ -z "$PRODUCT_ID" ] || [ "$PRODUCT_ID" == "null" ]; then
        echo "Failed to create product or extract ID"
        return 1
    fi

    curl -s -X GET "$BASE_URL/product/$PRODUCT_ID"
    echo -e "\nProduct retrieved."

    curl -s -X GET "$BASE_URL/product?query=$PRODUCT_NAME"
    echo -e "\nProduct search completed."

    curl -s -X DELETE "$BASE_URL/product/$PRODUCT_ID"
    echo -e "\nProduct deleted."
}

for (( i = 0; i < 100; i++ )); do
    echo "Test #$((i+1))"
    test_api
done
