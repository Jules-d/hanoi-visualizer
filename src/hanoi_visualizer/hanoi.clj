(ns hanoi-visualizer.hanoi
  (:require [clojure.string :as string])
  [:use clojure.test])

; (vec ("a","b"))
; java.lang.ClassCastException: java.lang.String cannot be cast to clojure.lang.IFn
; hanoi-visualizer\src\hanoi_visualizer\hanoi.clj:5 hanoi-visualizer.util/eval6989

; We need to not have strings in the first position of lists
; before we can treat it as clojure forms, so we need a macro to
; handle it without evaluating it.

(vec '("a","b"))

(defmacro tuple->vector [tuple]
  (vec tuple))


(def a-tuple (macroexpand (tuple->vector ("a","b"))))

(vec (map (partial apply (fn [from to]{:from from :to to}))
          (seq '[("a","c"), ("a","b"), ("c","b")])))

(defmacro read-hanoi [haskel-input]
  (vec (map (partial apply (fn [from to]{:from from :to to}))
          (seq haskel-input))))

(def answer (read-hanoi [("a","c"), ("a","b"), ("c","b")]))
(def pegs (set (flatten (map vals answer))))
(def a-move (first answer))

;*Main> hanoi 3 "a" "b" "c"
(def answer3 (read-hanoi [("a","b"),("a","c"),("b","c"),("a","b"),("c","a"),("c","b"),("a","b")]))

a-tuple
pegs
answer
a-move

(quote :a)
; This is what the output we want to visualize looks like:
; hanoi 2 "a" "b" "c"
; == [("a","c"), ("a","b"), ("c","b")]


; (map str [1 2 3 4])

; ultimate, I want a list of visualizable data: a list of an array of pegs, where a peg is a list of rings.

; so a peg is a list of numbers, where 1 is the smallest ring, 2 is the second smallest etc.
; the rules of the game should enforce that lists are sorted at all times.

(def space "_")

(def example-peg3 [1,2,3])
(defn init-peg [n] (range 1 (inc n)))

(is (== (init-peg 1)) [1])
(is (== (init-peg 2)) [1 2])
(is (== (init-peg 5)) [1 2 3 4 5])

(defn ring->string [n]
  (if (<= n 1) "()"
                    (apply str (concat "(" (repeat (* 2 (dec n)) space) ")"))))

;(is (== (ring->string 1) "()") ; Curious.  This failes to compile under heroku:
;       ERROR in clojure.lang.PersistentList$EmptyList@1 (Numbers.java:206)
;       expected: (== (ring->string 1) "()")
;         actual: java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number

(map ring->string (range 1 5))

(def test-state {"a" '(1) "b" '(2) "c" '(3)})
(def initial-state {"a" () "b" () "c" ()})

; We can't easily figure out how many rings the solution is for, so we'll have to infer it as we go along.

(apply concat (vals test-state))
(apply concat (vals initial-state))
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
                            [new-ring []])
                          [(first from-peg) (rest from-peg)])
        to-peg (cons ring to-peg)]
    (assoc pegs from from-peg to to-peg)))


(cons 1 ())
(cons 2 (cons 1 ()))
(pop '(1 2))
(peek '(1 2))
(cons 1 '(2 3))

a-move
(process-hanoi-move initial-state a-move)
(process-hanoi-move test-state a-move)
(-> initial-state
    (process-hanoi-move a-move)
    (process-hanoi-move a-move))

(reduce process-hanoi-move initial-state answer)

(reduce process-hanoi-move initial-state answer3)

