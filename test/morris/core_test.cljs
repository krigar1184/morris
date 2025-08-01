(ns morris.core-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [morris.core :as subject]))

(deftest get-mills-test
  (testing "Get mills from given coords")
  (let* [coords {[0 0] :black [0 3] :black [0 6] :black
                 [1 1] nil [1 3] nil [1 5] nil
                 [2 2] :white [2 3] nil [2 4] nil
                 [3 0] nil [3 1] nil [3 2] :white  [3 4] nil [3 5] nil [3 6] nil
                 [4 2] :white [4 3] nil [4 4] nil
                 [5 1] nil [5 3] nil [5 5] nil
                 [6 0] nil [6 3] nil [6 6] nil}]
        (is (= (subject/get-mills coords) {:white [[2 2] [3 2] [4 2]] :black [[0 0] [0 3] [0 6]]}))))
