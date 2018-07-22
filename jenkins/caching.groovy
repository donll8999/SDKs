def hasCache(gcloud, hash, id) {
  return gcloud.keyExists('cache-' + hash + '-' + id);
}

def checkPreloaded(gcloud, preloaded, hash, id, title) {
  if (hasCache(gcloud, hash, id)) {
    preloaded[id] = true;
    echo ('Preloadable ' + title + ' is already cached in Google Cloud')
  } else {
    preloaded[id] = false;
    echo ('Preloadable ' + title + ' is NOT cached in Google Cloud, will build this run...')
  }
}

def checkMultiplePreloaded(gcloud, preloaded, hash, ids, title) {
  def allInCache = true
  ids.each { id -> 
    if (!this.hasCache(gcloud, hash, id)) {
      allInCache = false;
    }
  }
  if (allInCache) {
    preloaded[id] = true;
    echo ('Preloadable ' + title + ' is already cached in Google Cloud')
  } else {
    preloaded[id] = false;
    echo ('Preloadable ' + title + ' is NOT cached in Google Cloud, will build this run...')
  }
}

def pullCacheDirectory(gcloud, hash, id, dir) {
  dir = dir.trim('/')
  if (env.NODE_NAME.startsWith("windows-")) {
    // This is running in Google Cloud, so we just pull the cache
    // directly onto the agent without going via Jenkins.
    bat ('gsutil -m cp "gs://redpoint-build-cache/' + hash + '/' + dir + '" "' + dir + '"')
  } else {
    // Try to unstash first in case Jenkins has already cached this.
    def wasUnstashSuccessful = false
    try {
      unstash name: ('cache-' + hash + '-' + id)
      wasUnstashSuccessful = true
    } catch (e) {
      wasUnstashSuccessful = false
    }

    if (wasUnstashSuccessful) {
      // Jenkins hasn't got a copy of this yet.
      googleStorageDownload bucketUri: ('gs://redpoint-build-cache/' + hash + '/' + dir + '/*'), credentialsId: 'redpoint-games-build-cluster', localDirectory: (dir + '/'), pathPrefix: (hash + '/' + dir + '/')
      stash includes: ('client_connect/sdk/' + it + '/**'), name: ('cache-' + hash + '-' + id)

      // We have just implicitly unstashed on this node, so nothing more to do here.
    }
  }
}

def pushCacheDirectory(gcloud, hash, id, dir) {
  dir = dir.trim('/')
  if (env.NODE_NAME.startsWith("windows-")) {
    // This is running in Google Cloud, so we just push the cache
    // directly onto the agent without going via Jenkins.
    bat ('gsutil -m cp "' + dir + '" "gs://redpoint-build-cache/' + hash + '/' + dir + '"')
    gcloud.keySet('cache-' + hash + '-' + id, 'true')
  } else {
    // Push from the agent via Jenkins.
    googleStorageUpload bucket: ('gs://redpoint-build-cache/' + hash), credentialsId: 'redpoint-games-build-cluster', pattern: (dir + '/**')

    // Now also stash the result so we can pull it later on Jenkins agents faster
    stash includes: (dir + '/**'), name: ('cache-' + hash + '-' + id)
  }
}

return this