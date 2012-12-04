(ns maze.views.index
  (:use [noir.core :only [defpage defpartial]]
        [noir.fetch.remotes :only [defremote]]
        [hiccup.core :only [html]] 
        [hiccup.page :only [include-css include-js html5]]
        [hiccup.element :only [javascript-tag]]
        [maze.generator]))

(defmulti to-number class)
(defmethod to-number Number [n] n)
(defmethod to-number :default [obj] (read-string obj))

; When using {:optimizations :whitespace}, the Google Closure compiler combines
; its JavaScript inputs into a single file, which obviates the need for a "deps.js"
; file for dependencies.  However, true to ":whitespace", the compiler does not remove
; the code that tries to fetch the (nonexistent) "deps.js" file.  Thus, we have to turn
; off that feature here by setting CLOSURE_NO_DEPS.
;
; Note that this would not be necessary for :simple or :advanced optimizations.
(defn include-clojurescript [path]
  (list
    (javascript-tag "var CLOSURE_NO_DEPS = true;")
      (include-js path)))

(defpartial layout [& content]
  (html5
    [:head
     [:title "Maze Generator"]
     (include-css "/css/default.css")
     (include-css "/css/spinner.css")
     (include-js "https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js")
     ;(include-js "https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.1/jquery-ui.min.js")
     ]
    [:body
     [:div#wrapper
      content]
      (include-clojurescript "/cljs/maze.js")
     ]))

(defpartial spinner [css-class]
  (html
    [:div#spinner {:class css-class }
      [:div {:class "spinner"}
       (for [x (range 1 13)]
          (html 
            [:div {:class (str "bar" x)}]))]]))

(defremote generate-maze [width height]
  (create-maze 
    rand-int
    (to-number width) 
    (to-number height)))

(defpage [:get "/"] {:as params}
  (layout
    (html
      [:canvas#world {:data-cell-size (get params :cell-size 10)}])))