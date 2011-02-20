(ns guiftw.events
  (:require (guiftw [props :as props]
		    [utils :as utils])
	    (clojure.contrib [string :as string])))

;;{:text "asdf"
;; :mouse+mouse-clicked (fn [event object by-id groups])}

(defrecord Event [spec handler listener f]
  props/Property
  (set-on [this subject] (f subject))
  (property-name [this] spec)
  (get-value [this] handler))

(defn event-spec? [spec]
  (some #{\+} (name spec)))

(defn listener&method-names [spec]
  {:pre [(event-spec? spec)]}

  (let [[listener method] (->> spec name (reduce str) (string/split #"\+"))]
    [(-> listener utils/CamelCase (str "Listener"))
     (-> method utils/camelCase-small)]))

(defmacro listener [spec handler]
  (let [[listener-name method] (listener&method-names spec)
	listener-class (symbol listener-name)]
    `(reify ~listener-class
	    (~(symbol method) [_# event#] (~handler event#))
	    
	    ;; Implement empty methods.
	    ;; Needed because reify would make them abstract.
	    ~@(for [m (->> listener-class resolve .getMethods
			   (map #(.getName %))
			   (filter #(not= % method))
			   (map symbol))]
		(list m ['_ '_] nil)))))

(defmacro adder [spec listener-object]
  `(fn [subject#] (~(->> spec listener&method-names
			 first (str ".add") symbol)
		   subject#
		   ~listener-object)))
	    
(defmacro event [spec handler]
  (let [listener-object `(listener ~spec ~handler)
	adder-object `(adder ~spec ~listener-object)]
    `(Event. ~spec ~handler ~listener-object ~adder-object)))