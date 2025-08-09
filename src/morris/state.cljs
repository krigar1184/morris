(ns morris.state
  (:require [reagent.core :refer [atom]]))

(defonce init-coords {[0 0] nil [0 3] nil [0 6] nil
                      [1 1] nil [1 3] nil [1 5] nil
                      [2 2] nil [2 3] nil [2 4] nil
                      [3 0] nil [3 1] nil [3 2] nil  [3 4] nil [3 5] nil [3 6] nil
                      [4 2] nil [4 3] nil [4 4] nil
                      [5 1] nil [5 3] nil [5 5] nil
                      [6 0] nil [6 3] nil [6 6] nil})

; -- STATE START
(def current-coords (atom init-coords))
(def game-status (atom :not-started))
(def player1 {:name (atom "player 1") :pieces-left (atom 9) :color :white})
(def player2 {:name (atom "player 2") :pieces-left (atom 9) :color :black})
(def turn (atom 0))
(def pieces-left-this-turn (atom 1))
(def piece-removed? (atom false))
; -- STATE END
