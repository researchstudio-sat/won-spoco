#!/bin/bash

set -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

export DOCKER_HOST= # add docker host, e.g. satvm01.researchstudio.at:2376 here
export DOCKER_TLS_VERIFY=1

if [ -z "$DOCKER_CERT_PATH" ]; then
    (>&2 echo "You need to set DOCKER_CERT_PATH to authenticate with the docker daemon!")
    exit 1
fi

export WON_NODE_BASE= # add node URL, e.g. https://node.matchat.org here
export API_USERNAME=asdf # mocked api only needs a user name

$DIR/deploy.sh