(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'me.fogus/rv)
(def description "Code conversations in Clojure regarding the application of pure search, reasoning, and query algorithms.")
(def version "0.0.14") ;; unreleased
(def class-dir "target/classes")
(def jar-file (format "target/%s.jar" (name lib)))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis{:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis (update-in @basis [:libs] dissoc 'org.clojure/clojure)
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn- pom-template [version]
  [[:description description]
   [:url "https://github.com/fogus/rv"]
   [:licenses
    [:license
     [:name "Eclipse Public License 2.0"]
     [:url "https://www.eclipse.org/legal/epl-2.0/"]]]
   [:developers
    [:developer
     [:name "Fogus"]]]
   [:scm
    [:url "https://github.com/fogus/rv"]
    [:connection "scm:git:https://github.com/fogus/rv.git"]
    [:developerConnection "scm:git:ssh:git@github.com:fogus/rv.git"]
    [:tag (str "v" version)]]])

(defn jar [_]
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis (update-in @basis [:libs] dissoc 'org.clojure/clojure)
                :src-dirs ["src"]
                :src-pom "no-such-pom.xml" ;; prevent default pom copying
                :pom-data (pom-template version)})
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn- jar-opts [opts]
  (println "\nVersion:" version)
  (assoc opts
         :lib lib   :version version
         :jar-file  jar-file
         :basis     (b/create-basis {})
         :class-dir class-dir
         :target    "target"
         :src-dirs  ["src"]
         :pom-data  (pom-template version)))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [{:keys [jar-file] :as opts} (jar-opts opts)]
    (dd/deploy {:installer :remote
                :sign-releases? false
                :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
