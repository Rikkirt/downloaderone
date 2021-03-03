/*

   Copyright 2021 EWS Solutions

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

package com.ewssolutions.downloaderone.util;

public enum PrefKeys {

    //KEY && DEFAULT VALUE
    DOWNLOAD_DIR("PREF_KEY_DOWNLOAD_DIR",System.getProperty("user.home")),
    //TOR_LOCATION("PREF_KEY_TOR_LOCATION", System.getProperty("home.user")+"/.local/share/torbrowser/tbb/x86_64/tor-browser_en-US/Browser/TorBrowser/Tor/tor"),
    TOR_LOCATION("PREF_KEY_TOR_LOCATION", System.getProperty("user.home")+"/Applications/tor-browser-linux64-8.5.4_en-US/tor-browser_en-US/Browser/start-tor-browser"),
    SOCKS_PROXY_HOST("PREF_KEY_SOCKS_PROXY_HOST","127.0.0.1"),
    SOCKS_PROXY_PORT("PREF_KEY_SOCKS_PROXY_PORT","9150"),
    USE_SOCKS_PROXY("PREF_KEY_USE_SOCKS_PROXY","true"),
    SHOW_DOWNLOAD_ERROR_MESSAGE("PREF_SHOW_DOWNLOAD_ERROR_MESSAGE","true"),
    YOUTUBE_DL_VERSION("PREF_YOUTUBE_DL_VERSION","0001.01.01");


    private String key;
    private String defaultValue;

    PrefKeys(String key , String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getKey(){
        return key;
    }

}
