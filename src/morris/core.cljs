(ns morris.core
  (:require [reagent.dom.client :as rdomc]))

(set! *warn-on-infer* true)

(defonce app-container (.getElementById js/document "app"))
(defonce root (delay (rdomc/create-root app-container)))

(def init-coords {'(0 0) false '(0 3) false '(0 6) false
                  '(1 1) false '(1 3) false '(1 5) false
                  '(2 2) false '(2 3) false '(2 4) false
                  '(3 0) false '(3 1) false '(3 2) false  '(3 4) false '(3 5) false '(3 6) false
                  '(4 2) false '(4 3) false '(4 4) false
                  '(5 1) false '(5 3) false '(5 5) false
                  '(6 0) false '(6 3) false '(6 6) false})

(defn board []
  [:div.container
   [:div.board
    (for [i (range 0 7) j (range 0 7)]
      (if (contains? init-coords (list i j))
        ^{:key (str i j)} [:div.square {:style {:text-align :left}}
                           [:span i j]]
        ^{:key (str i j)} [:div.square.empty]))]])

(defn ^:export ^:dev/after-load run []
  (rdomc/render @root [board]))
