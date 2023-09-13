#!/bin/bash

if (($# < 2)); then
    echo "-----------------------------------------------------------------------------------------------------"
    echo "--- You must enter 2 parameters"
    echo "--- <admin-login> <admin-pw>"
    echo "--- Example: "
    echo "--- sh updateMetadata.sh <admin-login> <admin-pw>"
    echo "--- The script first checks to see if any GUIDs need to be added to Collibra"
    echo "--- Then it updates metadata for each organization"
    echo "-----------------------------------------------------------------------------------------------------"
    exit 1
fi

#Assigning variables

userid=$1
userpw=$2


url="http://localhost:8081/api/setup"

echo $url

curl -u $1:$2 -X POST $url


url="http://localhost:8081/api/sync?orgName=<orgName>&orgId=<orgId>"

echo $url

curl -u $1:$2 -X POST $url


url="http://localhost:8081/api/sync?orgName=<orgName>&orgId=<orgId>

echo $url

curl -u $1:$2 -X POST $url


echo "Finished updating AtScale metadata for Collibra"