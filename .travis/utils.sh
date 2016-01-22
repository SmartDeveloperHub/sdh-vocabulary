#!/bin/bash
set -e

function backupMavenRepo() {
  if [ "$1" != "porcelain" ];
  then
    mkdir /tmp/cache-trick
    mv "$HOME/.m2/repository/org/smartdeveloperhub" /tmp/cache-trick/
  else
    echo "Skipped Maven Repo backup"
  fi
}

function restoreMavenRepo() {
  if [ "$1" != "porcelain" ];
  then
    mv /tmp/cache-trick/smartdeveloperhub "$HOME/.m2/repository/org/"
  else
	  echo "Skipped Maven Repo restoration"
  fi
}

function decryptKeys() {
  if [ "$#" != "2" ];
  then
    echo "ERROR: No encryption password specified."
    exit 2
  fi

  if [ "$1" != "porcelain" ];
  then
    echo "Decrypting private key..."
    openssl aes-256-cbc -pass pass:"$2" -in target/config/ci/secring.gpg.enc -out local.secring.gpg -d
    echo "Decrypting public key..."
    openssl aes-256-cbc -pass pass:"$2" -in target/config/ci/pubring.gpg.enc -out local.pubring.gpg -d
  else
    echo "Skipped key decription"
  fi
}

function unshallowRepo() {
  if [ "$1" != "porcelain" ];
  then
    if [[ -a .git/shallow ]];
    then
      echo "- Unshallowing Git repository..."
      git fetch --unshallow
    fi
  else
    echo "Skipped Git repository unshallowing"
  fi
}

function fail() {
  echo "ERROR: Unknown command '${1}'."
  exit 1
}

mode=$1
shift
if [ "$mode" = "porcelain" ];
then
  action=$1
  shift
else
  action=$mode
  mode=execute
fi

case "$action" in
  backup-maven-repo ) backupMavenRepo "$mode";;
  restore-maven-repo) restoreMavenRepo "$mode";;
  prepare-keys      ) decryptKeys "$mode" "$@";;
  prepare-repo      ) unshallowRepo "$mode";;
  *                 ) fail "$action";;
esac
