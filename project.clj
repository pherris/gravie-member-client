(defproject gravie-member-client "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [sablono "0.4.0"]
                 [org.omcljs/om "1.0.0-alpha19"]
                 [prismatic/om-tools "0.3.12"]
                 [camel-snake-kebab "0.3.2"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [cljs-ajax "0.5.1"]
                 [prismatic/schema "1.0.4"]

                 ;; Frontend tests
                 [com.cemerick/clojurescript.test "0.3.0"]
                 [org.clojure/tools.reader "0.9.2"]]

  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              {:dev
               {:source-paths ["src"]
                  ;; If no code is to be run, set :figwheel true for continued automagical reloading
                  :figwheel {:on-jsload "gravie-member-client.core/on-js-reload"
                             :websocket-url "wss://member-cljs.local.gravie.us/figwheel-ws"}

                  :compiler {:main gravie-member-client.core
                             :asset-path "/js/cljs/out"
                             :output-to "../gravie-train/gravie-member/web-app/js/cljs/gravie-member-client-debug.js"
                             :output-dir "../gravie-train/gravie-member/web-app/js/cljs/out"
                             :source-map-timestamp true}}

                ;; This next build is an compressed minified build for
                ;; production. You can build this with:
                ;; lein cljsbuild once min

               :min
               {:figwheel true
                :source-paths ["src"]
                :compiler {:output-to "../gravie-train/gravie-member/web-app/js/cljs/gravie-member-client.min.js"
                           :main gravie-member-client.core
                           :optimizations :advanced
                           :pretty-print false}}
              :test
               {:source-paths ["test-cljs" "src"]
                  :compiler {:main gravie-member-client.core
                             :output-to "target/gravie-member-client-debug.js"
                             :output-dir "target/"
                             :asset-path "base/target"
                             :source-map-timestamp true}}}}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 3449 ;; default
             :server-ip "member-cljs.local.gravie.us"

             :load-warninged-code true  ;; <- Add this
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
