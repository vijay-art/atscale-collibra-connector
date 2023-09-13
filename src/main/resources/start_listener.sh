#!/bin/bash

echo "Starting atscale-to-collibra-integration listener"

java -jar atscale-to-collibra-integration-1.0.0.jar \
    --server.port=8081 \
    --trigger.api.username=dummy \
    --trigger.api.password=dummy \
    --collibra.url=dummyUrl \
    --collibra.username=dummy \
    --collibra.password=\
    --atscale.api.dchost=dummyHost \
    --atscale.api.dcport= \
    --atscale.api.apihost=dummyHost \
    --atscale.api.apiport= \
    --atscale.api.authhost=dummyHost \
    --atscale.api.authport= \
    --atscale.api.username=dummy \
    --atscale.api.password=dummy \
    --atscale.api.disablessl=
