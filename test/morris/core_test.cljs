(ns morris.core-test
  (:require
   [cljs.test :refer [deftest is]]
   [morris.core :as subject]))

(deftest has-mill?-test
  (is (= false
         (subject/has-mill? 0 0))))
