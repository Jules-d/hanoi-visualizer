(ns hanoi-visualizer.hanoi
  (:require [clojure.string :as string])
  [:use clojure.test]
  (:use [clojure.pprint :only [pprint]]))

; (vec ("a","b"))
; java.lang.ClassCastException: java.lang.String cannot be cast to clojure.lang.IFn
; hanoi-visualizer\src\hanoi_visualizer\hanoi.clj:5 hanoi-visualizer.util/eval6989

; We need to not have strings in the first position of lists
; before we can treat it as clojure forms, so we need a macro to
; handle it without evaluating it.

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

(defn init-peg [n] (range 1 (inc n)))

(is (== (init-peg 1)) [1])
(is (== (init-peg 2)) [1 2])
(is (== (init-peg 5)) [1 2 3 4 5])66

(def space "_")

(defn ring->string [n]
  (if (<= n 1) "()"
    (apply str (concat "(" (repeat (* 2 (dec n)) space) ")"))))

(is (= (ring->string 1) "()"))

(defn peg->string
  "Render a peg (a sequence of ring values) as ascii art"
  [number-of-rings peg]
  (let [left-string (string/join "" (map #(if (some #{%} peg) "(" space) (reverse (range 1 (inc number-of-rings)))))
        right-string (string/reverse (string/replace left-string #"\(" ")" ))]
    (str left-string right-string)))

(is (= "((((()))))" (peg->string 5 [1 2 3 4 5])))
(is (= (str "(" space "(" space "()" space ")" space ")")
       (peg->string 5 [1  3  5])))

(defn abs
  "Simple numerical absolute value"
  [n]
  (if (neg? n) (- n) n))

(defn count-peg-moves [answer direction peg]
  (count (filter
          #(= peg (direction %)) answer)))

(def example-answer (read-hanoi [("a","c"), ("a","b"), ("c","b")]))
(def answer3 (read-hanoi [("a","b"),("a","c"),("b","c"),("a","b"),("c","a"),("c","b"),("a","b")]))
(is (= (count-peg-moves example-answer :from "a") 2))
(is (= (count-peg-moves example-answer :to "a") 0))
(is (= (count-peg-moves answer3 :from "a") 4))
(is (= (count-peg-moves answer3 :to "a") 1))

(defn infer-rings
  "Figure out how many rings the answer is solving for by tracking movements on the first peg (in two passes)"
  [answer]
  (let [first-peg (:from (ffirst answer))]
    (abs (- (count-peg-moves answer :from "a")
            (count-peg-moves answer :to "a")))))

(is (= (infer-rings example-answer) 2))
(is (= (infer-rings answer3) 3))

(def pegs (set (flatten (map vals example-answer))))


; TODO: We can build this
(defn answer->initial-state
  "Determine an initial state from an answer.

  Lots of Assumptions:
  Answer is complete
  Three pegs named \"a\" \"b\" and \"c\""
  [answer]
  (let [number-of-rings (infer-rings answer)
        peg-names (set (flatten (map vals answer)))
        num-pegs (count peg-names)
        pegs (reduce into (for [p peg-names] {p ()}))]
    (assoc pegs (ffirst pegs) (range 1 (inc num-pegs)))))

(def test-state {"a" '(1) "b" '(2) "c" '(3)})
(def test-initial-state {"a" '(1 2 3 4 5) "b" () "c" ()})
; TODO: calculate initial state

; take a map of pegs to a (potentially incomplete) list-of-rings, and a move,
; and retun the new state with a ring moved from the :from peg to the :to peg
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
    ; TODO: generate initial state
    (reductions process-hanoi-move initial-state answer)))

(defn state->string
  "Returns a list of acsii-art strings for each peg, intended to look like pegs from above"
  [num-rings state]
  (map (partial peg->string num-rings)
       (vals state)))

(def peg->string-5 (partial peg->string 5))

(map (partial state->string 5) (answer->history answer3))

; OK.
; Still important TODO's, but this is the shape of the solution:
(pprint (map (partial state->string 3) (answer->history answer3)))



