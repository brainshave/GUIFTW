(ns guiftw.swing.Canvas)

;; Class that represents canvas' drawing event
;;
;; Usage: get Graphics object using getGraphics method and widget on
;; which painting happens using getCompononent method.
(gen-class :name guiftw.swing.CanvasEvent
           :extends java.awt.event.ComponentEvent
           :prefix event-
           :init init
           :state graphics
           :constructors {[java.awt.Component java.awt.Graphics]
                          [java.awt.Component int]}
           :methods [[getGraphics [] java.awt.Graphics]])

(defn event-init [source graphics]
  [[source java.awt.event.ComponentEvent/PAINT_EVENT_MASK]
   graphics])

(defn event-getGraphics [this]
  (.graphics this))

;; Interface for capturing CanvasEvents:
(gen-interface :name guiftw.swing.CanvasListener
               :extends [java.util.EventListener]
               :methods [[paint [guiftw.swing.CanvasEvent] Object]])

;; Canvas class
;;
;; Usage:
;; (swing [Canvas [:canvas+paint (fn [state event]
;;                                 (.drawSmth (.getGraphics event)))]])
(gen-class :name guiftw.swing.Canvas
           :extends javax.swing.JPanel
           :main false
           :prefix canvas-
           :init init
           :state painters
           :methods [[addCanvasListener [guiftw.swing.CanvasListener] Object]
                     [removeCanvasListener [guiftw.swing.CanvasListener] Object]
                     [getCanvasListeners [] clojure.lang.PersistentVector]])

(defn canvas-init [& args]
  [args (atom [])])

(defn canvas-addCanvasListener [this listener]
  (swap! (.painters this) conj listener))

(defn canvas-removeCanvasListener [this listener]
  (swap! (.painters this)
         #(into [] (filter (fn [x] (not= x %2)) %1))
         listener))

(defn canvas-getCanvasListeners [this]
  @(.painters this))

(defn canvas-paintComponent [this graphics]
  (let [event (guiftw.swing.CanvasEvent. this graphics)]
    (doseq [painter @(.painters this)]
      (.paint painter event))))
