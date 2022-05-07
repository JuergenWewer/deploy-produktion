#!/bin/bash
buildctl build --frontend dockerfile.v0 --local context=. --local dockerfile=. --output type=image,name=jw-cloud.org:18443/csi-raid:v0.0.44,push=true