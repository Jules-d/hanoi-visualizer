(ns hanoi-visualizer.hanoi
  (:require [clojure.string :as string])
  [:use clojure.test]
  (:use [clojure.pprint :only [pprint]]))

; We need to not have strings in the first position of lists
; before we can treat it as clojure forms, so we need a macro to
; handle it without evaluating it.  Otherwise you get an error like:
; (vec ("a","b"))
; java.lang.ClassCastException: java.lang.String cannot be cast to clojure.lang.IFn
; hanoi-visualizer\src\hanoi_visualizer\hanoi.clj:5 hanoi-visualizer.util/eval6989

(defmacro read-hanoi
  "You need a macro evaluate haskel inside lighttable, otherwise it tries to call a string as a function"
  [haskel-input]
  (vec (map (partial apply (fn [from to]{:from from :to to}))
            (seq haskel-input))))

(defn read-fn [input]
  (vec (map (partial apply (fn [from to]{:from from :to to}))
            (seq input))))

; This is what the output we want to visualize looks like:
; hanoi 2 "a" "b" "c"
; == [("a","c"), ("a","b"), ("c","b")]

; ultimately, I want a list of visualizable data: a list of an array of pegs, where a peg is a list of rings.
; so a peg is a list of numbers, where 1 is the smallest ring, 2 is the second smallest etc.
; the rules of the game should enforce that lists are sorted at all times.

(def space "_")

(defn init-peg [n] (range 1 (inc n)))

(defn ring->string [n]
  (if (<= n 1) "()"
    (apply str (concat "(" (repeat (* 2 (dec n)) space) ")"))))

(defn peg->string
  "Render a peg (a sequence of ring values) as ascii art"
  [number-of-rings peg]
  (let [left-string (string/join "" (map #(if (some #{%} peg) "(" space) (reverse (range 1 (inc number-of-rings)))))
        right-string (string/reverse (string/replace left-string #"\(" ")" ))]
    (str left-string right-string)))

(defn abs
  "Simple numerical absolute value"
  [n]
  (if (neg? n) (- n) n))

(defn count-peg-moves [answer direction peg]
  (count (filter
          #(= peg (direction %)) answer)))

(def example-answer (read-hanoi [("a","c"), ("a","b"), ("c","b")]))
(def answer3 (read-hanoi [("a","b"),("a","c"),("b","c"),("a","b"),("c","a"),("c","b"),("a","b")]))

(defn infer-rings
  "Figure out how many rings the answer is solving for by tracking movements on the first peg (in two passes)"
  [answer]
  (let [first-peg (:from (ffirst answer))]
    (abs (- (count-peg-moves answer :from "a")
            (count-peg-moves answer :to "a")))))

(def pegs (set (flatten (map vals example-answer))))

(defn answer->initial-state
  "Determine an initial state from an answer."
  [answer]
  (let [number-of-rings (infer-rings answer)
        peg-names (set (flatten (map vals answer)))
        num-pegs (count peg-names)
        pegs (reduce into (for [p peg-names] {p ()}))]
    (assoc pegs (ffirst pegs) (range 1 (inc num-pegs)))))

; take a state and a move and retun the new state with a ring moved from the
; :from peg to the :to peg
; Note that new pegs and rings are created if necessary.
(defn process-hanoi-move [pegs move]
  (let [from (:from move)
        to (:to move)
        to-peg (pegs to)
        from-peg (pegs from)
        [ring from-peg] (if (empty? from-peg)
                          (let [rings (apply concat (vals pegs))
                                new-ring (inc (count rings))]
                            [new-ring ()])
                          [(first from-peg) (rest from-peg)])
        to-peg (cons ring to-peg)]
    (assoc pegs from from-peg to to-peg)))

(defn answer->history
  "Take an answer and generate a lazy sequence of all the intermediate states"
  [answer]
  (let [initial-state (answer->initial-state answer)]
    (reductions process-hanoi-move initial-state answer)))

(defn state->string
  "Returns a list of acsii-art strings for each peg, intended to look like pegs from above"
  [num-rings state]
  (map (partial peg->string num-rings)
       (vals state)))

; Some simple tests
(def test-state {"a" '(1) "b" '(2) "c" '(3)})
(def test-initial-state {"a" '(1 2 3 4 5) "b" () "c" ()})

(is (== (init-peg 1)) [1])
(is (== (init-peg 2)) [1 2])
(is (== (init-peg 5)) [1 2 3 4 5])66

(is (= (ring->string 1) "()"))

(is (= "((((()))))" (peg->string 5 [1 2 3 4 5])))
(is (= (str "(" space "(" space "()" space ")" space ")")
       (peg->string 5 [1  3  5])))

(is (= (count-peg-moves example-answer :from "a") 2))
(is (= (count-peg-moves example-answer :to "a") 0))
(is (= (count-peg-moves answer3 :from "a") 4))
(is (= (count-peg-moves answer3 :to "a") 1))

(is (= (infer-rings example-answer) 2))
(is (= (infer-rings answer3) 3))

