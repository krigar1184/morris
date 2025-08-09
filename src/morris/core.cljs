(ns morris.core
  (:require
   [cljs.core :as c]
   [morris.state :as state]
   [morris.utils :refer [log]]
   [reagent.dom.client :as rdomc]))

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

(defn- init-game []
  (reset! state/game-status :started))

(defn- reset-game [_]
  (reset! state/current-coords state/init-coords)
  (reset! state/turn 1)
  (reset! state/game-status :placing)
  (reset! (get state/player1 :pieces-left) 9)
  (reset! (get state/player2 :pieces-left) 9)
  (reset! state/pieces-left-this-turn 1)
  (reset! state/piece-removed? false))

(defn- get-current-player [turn]
  (if (odd? turn) state/player1 state/player2))

(defn- is-mill? [cmp coords]
  (log coords)
  (contains? possible-mills (sort-by cmp coords)))

(defn- partition-mills [cmp mills]
  (log mills)
  (loop [i 0 acc []]
    (if (>= i (count mills)) (filter #(is-mill? cmp %) acc)
        (let [m (nth mills i)
              parts (partition 3 1 m)]
          (recur
           (inc i)
           (apply conj acc
                  (filter #(is-mill? cmp %) parts)))))))

(defn- get-mills-from-color-group [coords]
  (let [rows (->> coords
                  (group-by first)
                  vals
                  (filter #(>= (count %) 3))
                  (mapv #(sort-by second %))
                  (partition-mills second))
        cols (->> coords
                  (group-by second)
                  vals
                  (filter #(>= (count %) 3))
                  (mapv #(sort-by first %))
                  (partition-mills second))]
    (->> (concat cols rows)
         (map #(map vec %)))))

(defn get-mills [coords]
  (let [grouped-fields (group-by second coords)
        whites (:white grouped-fields)
        blacks (:black grouped-fields)
        mw (or (->> whites keys get-mills-from-color-group (mapv vec)) [])
        mb (or (->> blacks keys get-mills-from-color-group (mapv vec)) [])]
    {:white mw
     :black mb}))

(defn can-remove? [mills]
  (if @state/piece-removed? false
      (> (count mills) 0)))

(defn- place-or-remove-piece [i j mills]
  (if (= 0 (+ @(get state/player1 :pieces-left) @(get state/player2 :pieces-left)))
    (reset! state/game-status :moving)
    (let [key [i j]
          player (get-current-player @state/turn)]
      (cond
        (and (= 0 @state/pieces-left-this-turn) (can-remove? (get mills (get player :color))))
        (do (swap! state/current-coords assoc key nil)
            (swap! state/piece-removed? not))
        (not (nil? (get @state/current-coords key))) nil
        (= 0 @state/pieces-left-this-turn) nil
        :else (do (swap! state/current-coords assoc key (get player :color))
                  (swap! state/pieces-left-this-turn dec))))))

(defn- ui-next-turn []
  (let [player (get-current-player @state/turn)]
    (swap! (get player :pieces-left) dec)
    (swap! state/turn inc)
    (swap! state/piece-removed? not)
    (reset! state/pieces-left-this-turn 1)))

(defn- ui-board []
  (let [coords @state/current-coords
        player (get-current-player @state/turn)
        current-mills (get-mills @state/current-coords)]
    [:div [:div.header
           [:p (str "game status" @state/game-status)]
           [:p (str @(get player :name) " turn")]
           [:p (str "pieces left: " @(get player :pieces-left))]
           [:p (str "White mills: " (get current-mills :white))]
           [:p (str "Black mills: " (get current-mills :black))]
           (when (can-remove? (get current-mills (get player :color)))
             [:p "You can remove a piece."])]
     [:div.board
      (for [i (range 0 7) j (range 0 7)]
        (let [k (list i j)]
          (if (contains? coords k)
            ^{:key (str i j)} [:div.square
                               {:on-click #(apply place-or-remove-piece [i j current-mills])}
                               (if (nil? (get coords k)) \u25cf
                                   [:span {:style {:color (get coords k)}} \u25c9])]
            ^{:key (str i j)} [:div.square.empty])))]
     [:br]
     [:input {:type :button :value "Next turn" :on-click ui-next-turn}]]))

(defn- set-player-name [event] ; TODO refactor
  (if (= "player1_name" (-> event .-target .-name))
    (reset! (get state/player1 :name) (-> event .-target .-value))
    (reset! (get state/player2 :name) (-> event .-target .-value))))

(defn- ui-start []
  [:div.container
   (cond (= :not-started @state/game-status)
         [:div.start
          [:input {:type :button
                   :value "Start"
                   :on-click #(init-game)}]]
         (= :started @state/game-status)
         [:div
          [:label {:id "player1_name"}]
          [:input {:name "player1_name" :default-value @(get state/player1 :name) :type :text :on-blur set-player-name}]
          [:br] [:br]
          [:label {:id "player2_name"}]
          [:input {:name "player2_name" :default-value @(get state/player2 :name) :type :text :on-blur set-player-name}]
          [:br] [:br] [:input {:type :button :value "Play!" :on-click #(do (swap! state/turn inc) (reset! state/game-status :placing))}]]
         (or (= :placing @state/game-status) (= :moving @state/game-status)) [:div [ui-board]
                                                                              [:br]
                                                                              [:div.footer
                                                                               [:input {:type :button :value "Reset game" :on-click reset-game}]]])])

(defn ^:export ^:dev/after-load run []
  (rdomc/render @root [ui-start]))

