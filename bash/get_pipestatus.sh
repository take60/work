#!/bin/bash

true | true | false
[ -n "$(echo ${PIPESTATUS[@]} | tr -d '0 ')" ] && exit 1

