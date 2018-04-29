(def project 'hiccup2html)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.9.0"]
                            [hiccup "1.0.5"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(task-options!
 aot {:namespace   #{'hiccup2html.core}}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/hiccup2html"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 repl {:init-ns    'hiccup2html.core}
 jar {:main        'hiccup2html.core
      :file        (str "hiccup2html-" version "-standalone.jar")})

(deftask compile-all-aot
  "compile all namespaces aot"
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot :all true)
          (pom)
          (uber)
          (target :dir dir))))

(deftask build-native-image-graalvm
  "Build a native image from existing AOT classes"
  []
  (with-pass-thru fs
    (println "Building native image")
    (println (:out (clojure.java.shell/sh
      "native-image"
      "-H:+ReportUnsupportedElementsAtRuntime"
      "-H:Name=hiccup2html"
      "-cp" ".:target"
      "hiccup2html.core")))))

(deftask build-native-image
  "AOT compile namespaces and then build image with GraalVM's `native-imamge`"
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (comp (compile-all-aot :dir dir)
        (build-native-image-graalvm)))


(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (with-pass-thru fs
    (require '[hiccup2html.core :as app])
    (apply (resolve 'app/-main) args)))

(require '[adzerk.boot-test :refer [test]])
