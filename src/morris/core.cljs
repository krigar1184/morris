(ns morris.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdomc]
            [cljs.core :as c]))

(set! *warn-on-infer* true)

(defonce app-container (.getElementById js/document "app"))
(defonce root (delay (rdomc/create-root app-container)))
(defonce possible-mills {; horizontal
                         [[0 0] [0 3] [0 6]] true
                         [[1 1] [1 3] [1 5]] true
                         [[2 2] [2 3] [2 4]] true
                         [[3 0] [3 1] [3 2]] true
                         [[3 4] [3 5] [3 6]] true
                         [[4 2] [4 3] [4 4]] true
                         [[5 1] [5 3] [5 5]] true
                         [[6 0] [6 3] [6 6]] true
                     ; vertical
                         [[0 0] [3 0] [6 0]] true
                         [[1 1] [3 1] [5 1]] true
                         [[2 2] [3 2] [4 2]] true
                         [[0 3] [1 3] [2 3]] true
                         [[4 3] [5 3] [6 3]] true
                         [[2 4] [3 4] [4 4]] true
                         [[1 5] [3 5] [5 5]] true
                         [[0 6] [3 6] [6 6]] true})
(defonce init-coords {[0 0] nil [0 3] nil [0 6] nil
                      [1 1] nil [1 3] nil [1 5] nil
                      [2 2] nil [2 3] nil [2 4] nil
                      [3 0] nil [3 1] nil [3 2] nil  [3 4] nil [3 5] nil [3 6] nil
                      [4 2] nil [4 3] nil [4 4] nil
                      [5 1] nil [5 3] nil [5 5] nil
                      [6 0] nil [6 3] nil [6 6] nil})

; -- STATE START
(def current-coords (r/atom init-coords))
(def game-status (r/atom :not-started))
(def player1 {:name (r/atom "player 1") :pieces-left (r/atom 9) :color :white})
(def player2 {:name (r/atom "player 2") :pieces-left (r/atom 9) :color :black})
(def turn (r/atom 0))
(def pieces-left-this-turn (r/atom 1))
(def piece-removed? (r/atom false))
; -- STATE END

(defn- init-game []
  (reset! game-status :started))

(defn- reset-game [_]
  (reset! current-coords init-coords)
  (reset! turn 1)
  (reset! game-status :placing)
  (reset! (get player1 :pieces-left) 9)
  (reset! (get player2 :pieces-left) 9)
  (reset! pieces-left-this-turn 1)
  (reset! piece-removed? false))

(defn- get-current-player [turn]
  (if (odd? turn) player1 player2))

(defn- is-mill? [cmp coords]
  (contains? possible-mills (sort-by cmp coords)))

(defn- partition-mills [cmp mills]
  (loop [i 0 acc []]
    (if (>= i (count mills)) (filter #(is-mill? cmp %) acc)
        (let [m (nth mills i)
              [a b] (split-at 3 m)]
          (recur (inc i) (conj acc a b))))))

(defn- get-mills-from-color-group [coords]
  (let* [cols (->> coords
                   (group-by first)
                   vals
                   (filter #(>= (count %) 3))
                   (mapv #(sort-by second %))
                   (remove empty?)
                   (partition-mills second))
         rows (->> coords
                   (group-by second)
                   vals
                   (filter #(>= (count %) 3))
                   (mapv #(sort-by first %))
                   (remove empty?)
                   (partition-mills second))]
        (->> (concat rows cols)
             (map #(map vec %)))))

(defn get-mills [coords]
  (let* [grouped-fields (group-by second coords)
         whites (:white grouped-fields)
         blacks (:black grouped-fields)
         mw (or (->> whites keys get-mills-from-color-group (mapv vec)) [])
         mb (or (->> blacks keys get-mills-from-color-group (mapv vec)) [])]
        {:white mw
         :black mb}))

(defn can-remove? [mills]
  (if @piece-removed? false
      (> (count mills) 0)))

(defn- place-or-remove-piece [i j mills]
  (if (= (+ @(get player1 :pieces-left) @(get player2 :pieces-left)) 0)
    (reset! game-status :moving)
    (let* [key [i j]
           player (get-current-player @turn)]
          (cond
            (and (= 0 @pieces-left-this-turn) (can-remove? (get mills (get player :color))))
            (do (swap! current-coords assoc key nil)
                (swap! piece-removed? not))
            (not (nil? (get @current-coords key))) nil
            (= @pieces-left-this-turn 0) nil
            :else (do (swap! current-coords assoc key (get player :color))
                      (swap! pieces-left-this-turn dec))))))

(defn- ui-next-turn []
  (let* [player (get-current-player @turn)]
        (swap! (get player :pieces-left) dec)
        (swap! turn inc)
        (swap! piece-removed? not)
        (reset! pieces-left-this-turn 1)))

(defn- ui-board []
  (let* [coords @current-coords
         player (get-current-player @turn)
         current-mills (get-mills @current-coords)]
        [:div [:div.header
               [:p (str "game status" @game-status)]
               [:p (str @(get player :name) " turn")]
               [:p (str "pieces left: " @(get player :pieces-left))]
               [:p (str "White mills: " (get current-mills :white))]
               [:p (str "Black mills: " (get current-mills :black))]
               (when (can-remove? (get current-mills (get player :color)))
                 [:p "You can remove a piece."])]
         [:div.board
          (for [i (range 0 7) j (range 0 7)]
            (let* [k (list i j)]
                  (if (contains? coords k)
                    ^{:key (str i j)} [:div.square
                                       {:on-click #(apply place-or-remove-piece [i j current-mills])}
                                       (if (nil? (get coords k)) \u25cf
                                           [:span {:style {:color (get coords k)}} \u25c9])]
                    ^{:key (str i j)} [:div.square.empty])))]
         [:br]
         [:input {:type :button :value "Next turn" :on-click ui-next-turn}]]))

(defn- set-player-name [event] ; TODO refactor
  (if (= (-> event .-target .-name) "player1_name")
    (reset! (get player1 :name) (-> event .-target .-value))
    (reset! (get player2 :name) (-> event .-target .-value))))

(defn- ui-start []
  [:div.container
   (cond (= @game-status :not-started)
         [:div.start
          [:input {:type :button
                   :value "Start"
                   :on-click #(init-game)}]]
         (= @game-status :started)
         [:div
          [:label {:id "player1_name"}]
          [:input {:name "player1_name" :default-value @(get player1 :name) :type :text :on-blur set-player-name}]
          [:br] [:br]
          [:label {:id "player2_name"}]
          [:input {:name "player2_name" :default-value @(get player2 :name) :type :text :on-blur set-player-name}]
          [:br] [:br] [:input {:type :button :value "Play!" :on-click #(do (swap! turn inc) (reset! game-status :placing))}]]
         (or (= @game-status :placing) (= @game-status :moving)) [:div [ui-board]
                                                                  [:br]
                                                                  [:div.footer
                                                                   [:input {:type :button :value "Reset game" :on-click reset-game}]]])])

(defn ^:export ^:dev/after-load run []
  (rdomc/render @root [ui-start]))

