(ns maze.views.index
  (:use [noir.core :only [defpage defpartial]]
        [noir.fetch.remotes :only [defremote]]
        [hiccup.core :only [html]]
        [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [javascript-tag]]
        [maze.generator]
        [maze.solver]
    :require [clojure.core.cache :as cache]))

(def C (atom (cache/fifo-cache-factory {})))

(defmulti to-number class)
(defmethod to-number Number [n] n)
(defmethod to-number :default [obj] (read-string obj))

(defpartial layout [& content]
  (html5
    [:head
     [:title "Maze Generator"]
     (include-css "/css/default.css")
     (include-css "/css/spinner.css")
     (include-css "/css/ribbon.css")
     (include-js "https://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js")
     (include-js "/cljs/maze.js")]
    [:body
     [:div#wrapper content] ]))

(defpartial spinner [css-class]
  (html
    [:div#spinner {:class css-class }
      [:div {:class "spinner"}
       (for [x (range 1 13)]
          (html
            [:div {:class (str "bar" x)}]))]]))

(defpartial ribbon [text href]
  (html
    [:div#ribbon
      [:p
        [:a {:href href :title href :rel "me"} text]]]))

(defremote generate-maze [width height]
  (let [w    (to-number width)
        h    (to-number height)
        id   (java.util.UUID/randomUUID)
        maze (assoc (create-maze rand-int w h) :id id)]
    (swap! C cache/miss id maze)
    maze))

(defremote solve [id points]
  (let [maze (cache/lookup @C id)
        f (fn [[x y]] (shortest-path maze x y))]
    (vec (pmap f points))))

(defpage [:get "/"] {:as params}
  (layout
    (html
      [:div
        (spinner "container grey")
        (ribbon "Fork me on GitHub!" "https://github.com/rm-hull/maze")
        [:canvas#world
          { :data-cell-size (get params :cell-size 10)
            :data-draw      (get params :draw "")
            :data-count     (get params :count 1) }]])))
