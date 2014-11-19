(ns garden.units-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [clojure.test.check.clojure-test :refer [defspec]]
   [garden.units :as u]))

(u/defunit six-pack)
(u/defunit beer)
(u/add-conversion! :six-pack :beer 6)


(defspec addition-of-args-w-measurement-is-eq-to-unit-of-addition-w-nums
  (prop/for-all [x gen/int
                 y gen/int]
    (= (beer (+ x y))
       (beer (u/+ x y))
       (u/+ (beer x)
            (beer y)))))

(defspec multiplication-of-args-w-measurement-is-eq-to-unit-of-multiplication-w-nums
  (prop/for-all [x gen/int
                 y gen/int]
    (= (beer (* x y))
       (beer (u/* x y))
       (u/* (beer x)
            (beer y)))))

(defspec subtraction-of-args-w-measurement-is-eq-to-unit-of-subtraction-w-nums
  (prop/for-all [x gen/int
                 y gen/int]
    (= (beer (- x y))
       (beer (u/- x y))
       (u/- (beer x)
            (beer y)))))

(defspec division-of-args-w-measurement-is-eq-to-unit-of-division-w-nums
  (prop/for-all [x gen/int
                 y (gen/such-that (complement zero?) gen/int)]
    (= (beer (/ x y))
       (beer (u// x y))
       (u// (beer x)
            (beer y)))))

(defspec round-trip
  (prop/for-all [x gen/int]
    (let [sp1 (six-pack 1)
          sp2 (six-pack (beer (six-pack 1)))]
     (and (== (u/magnitude sp1)
              (u/magnitude sp2))
          (= (u/measurement sp1)
             (u/measurement sp2))))))

(defspec string-addition-is-equivalent-to-unit-addition
  (prop/for-all [x gen/int]
    (= (u/+ x (str x "px"))
       (u/+ x (u/px x)))))