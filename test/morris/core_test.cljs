(ns morris.core-test
  (:require
   [cljs.test :refer [deftest is testing]]
   [morris.core :as subject]))

(deftest get-mills-test
  (testing "Get mills from given coords - simple case 1")
  (let* [coords {[0 0] :black [0 3] :black [0 6] :black
                 [1 1] nil [1 3] nil [1 5] nil
                 [2 2] :white [2 3] nil [2 4] nil
                 [3 0] nil [3 1] nil [3 2] :white  [3 4] nil [3 5] nil [3 6] nil
                 [4 2] :white [4 3] nil [4 4] nil
                 [5 1] nil [5 3] nil [5 5] nil
                 [6 0] nil [6 3] nil [6 6] nil}]
        (is (= (subject/get-mills coords) {:white [[[2 2] [3 2] [4 2]]] :black [[[0 0] [0 3] [0 6]]]})))
  (testing "Get mills from given coords - simple case 2")
  (let* [coords {[4 3] nil, [3 4] nil, [0 0] :white,
                 [5 3] nil, [2 2] nil, [1 5] :black,
                 [3 2] nil, [2 4] nil, [4 2] nil,
                 [1 3] :black, [5 5] nil, [2 3] nil,
                 [3 1] nil, [5 1] nil, [3 0] nil,
                 [0 6] :white, [1 1] :black, [6 0] nil,
                 [6 3] nil, [3 5] nil, [6 6] nil,
                 [0 3] :white, [3 6] nil, [4 4] nil}]
        (is (= (subject/get-mills coords) {:white [[[0 0] [0 3] [0 6]]] :black [[[1 1] [1 3] [1 5]]]})))
  (testing "Get mills from given coords - simple case 3")
  (let* [coords
         {[4 3] nil, [3 4] :black, [0 0] nil,
          [5 3] nil, [2 2] nil, [1 5] nil,
          [3 2] :white, [2 4] nil, [4 2] nil,
          [1 3] nil, [5 5] nil, [2 3] nil,
          [3 1] :white, [5 1] nil, [3 0] :white,
          [0 6] nil, [1 1] nil, [6 0] nil,
          [6 3] nil, [3 5] :black, [6 6] nil,
          [0 3] nil, [3 6] :black, [4 4] nil}]
        (is (= (subject/get-mills coords) {:white [[[3 0] [3 1] [3 2]]] :black [[[3 4] [3 5] [3 6]]]})))
  (testing "Get mills from given coords - diagonals")
  (let* [coords
         {[4 3] nil, [3 4] nil, [0 0] :white,
          [5 3] nil, [2 2] :white, [1 5] nil,
          [3 2] nil, [2 4] nil, [4 2] nil,
          [1 3] nil, [5 5] :white, [2 3] nil,
          [3 1] nil, [5 1] nil, [3 0] nil,
          [0 6] nil, [1 1] :black, [6 0] nil,
          [6 3] nil, [3 5] nil, [6 6] :black,
          [0 3] nil, [3 6] nil, [4 4] :black}]
        (is (= (subject/get-mills coords) {:white [] :black []})))
  (let* [coords
         {[4 3] nil, [3 4] nil, [0 0] :white,
          [5 3] nil, [2 2] :white, [1 5] nil,
          [3 2] nil, [2 4] nil, [4 2] nil,
          [1 3] nil, [5 5] :black, [2 3] nil,
          [3 1] nil, [5 1] nil, [3 0] nil,
          [0 6] nil, [1 1] :white, [6 0] nil,
          [6 3] nil, [3 5] nil, [6 6] :black,
          [0 3] nil, [3 6] nil, [4 4] :black}]
        (is (= (subject/get-mills coords) {:white [] :black []})))
  (let* [coords
         {[4 3] nil, [3 4] nil, [0 0] :white,
          [5 3] nil, [2 2] nil, [1 5] :black,
          [3 2] nil, [2 4] nil, [4 2] nil,
          [1 3] :black, [5 5] nil, [2 3] nil,
          [3 1] :black, [5 1] :black, [3 0] :white,
          [0 6] :white, [1 1] :black, [6 0] :white,
          [6 3] nil, [3 5] nil, [6 6] nil,
          [0 3] :white, [3 6] nil, [4 4] nil}]
        (is (= (subject/get-mills coords) {:white [[[0 0] [0 3] [0 6]] [[0 0] [3 0] [6 0]]]
                                           :black [[[1 1] [1 3] [1 5]] [[1 1] [3 1] [5 1]]]})))
  (let* [coords
         {[4 3] nil, [3 4] :black, [0 0] nil,
          [5 3] nil, [2 2] nil, [1 5] nil,
          [3 2] :white, [2 4] nil, [4 2] nil,
          [1 3] nil, [5 5] nil, [2 3] nil,
          [3 1] :black, [5 1] nil, [3 0] :white,
          [0 6] nil, [1 1] nil, [6 0] nil,
          [6 3] nil, [3 5] :white, [6 6] nil,
          [0 3] nil, [3 6] :black, [4 4] nil}]
        (is (= (subject/get-mills coords) {:white [] :black []}))))
