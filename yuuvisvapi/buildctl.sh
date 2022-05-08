#!/bin/bash
buildctl build --frontend dockerfile.v0 --local context=. --local dockerfile=. --output type=image,name=jw-cloud.org:18443/yuuvis-v-api:3.0.19-SNAPSHOT,push=true
