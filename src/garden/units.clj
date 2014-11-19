(ns garden.units
  (:refer-clojure :exclude [rem + * - /])
  (:require
   [clojure.core :as clj]
   [clojure.string :as string]))

;; ---------------------------------------------------------------------
;; Protocols

(defprotocol IMagnitude
  (-magnitude [this]))

(defprotocol IMeasurement
  (-measurement [this]))

(defprotocol IUnit
  (-unit [this]))


;; ---------------------------------------------------------------------
;; Types

(defrecord Unit [magnitude measurement]
  IUnit
  (-unit [this] this)

  IMeasurement
  (-measurement [this]
    (if-let [m (.-measurement this)]
      (keyword m)))

  IMagnitude
  (-magnitude [this]
    (.-magnitude this)))


;; ---------------------------------------------------------------------
;; Functions

(defn magnitude [x]
  {:post [(number? %)]}
  (-magnitude x))

(defn measurement [x]
  (when-let [m (-measurement x)]
    (keyword m)))

(defn unit [x]
  {:post [(instance? Unit %)]}
  (-unit x))

(defmacro defunit
  ([sym]
     (let [sk (keyword sym)]
       `(defn ~sym [x#]
          (let [mg# (magnitude x#)
                ms# (measurement x#)
                i# (convert mg# ms# ~sk)]
            (Unit. i# ~sk)))))
  ([sym rep]
     (let [sk (keyword rep)]
       `(defn ~sym [x#]
          (let [mg# (magnitude x#)
                ms# (measurement x#)
                i# (convert mg# ms# ~sk)]
            (Unit. i# ~rep))))))

;;; Conversion

(def
  ^{:private true
    :doc "Map associating unit types to their conversion values."}
  conversion-table
  (atom {}))

(defn add-conversion! [m1 m2 amt]
  (let [m1k (keyword m1)
        m2k (keyword m2)]
    (swap! conversion-table
           (fn [ct]
             (-> ct
                 (assoc-in [m1k m2k] amt)
                 (assoc-in [m2k m1k] (/ 1.0 amt)))))))

(defn get-conversion [m1 m2]
  (get-in @conversion-table [m1 m2]))

(defn convert [amt m1 m2]
  (if (or (= m1 m2)
          (nil? m1)
          (nil? m2))
    amt
    (if-let [c (get-conversion m1 m2)]
      (* c amt)
      (throw (ex-info (str "Unable to convert measurement "
                           (pr-str m1)
                           " to "
                           (pr-str m2))
                      {:given [amt m1 m2]})))))


;;; Arithemetic

(defn ^Unit +
  "Return the sum of units. The leftmost summand with a non-nil unit
  of measurement determines the resulting unit's measurement value. If
  none of the summands have a measurement value the resulting unit
  will be without measurement. 

  Example:

    (+)
    => #garden.units.Unit{:magnitude 0, :measurement nil}

    (+ 1)
    => #garden.units.Unit{:magnitude 1, :measurement nil}

    (+ 1 (px 5))
    => #garden.units.Unit{:magnitude 6, :measurement :px}

    (+ (cm 5) (in 5) (mm 5))
    => #garden.units.Unit{:magnitude 18.2, :measurement :cm}"
  ([]
     (unit 0))
  ([u]
     (unit u))
  ([u1 u2]
     (let [mg1 (magnitude u1)
           mg2 (magnitude u2)
           ms1 (or (measurement u1)
                   (measurement u2))
           ms2 (or (measurement u2)
                   (measurement u1))
           mg3 (convert mg2 ms2 ms1)]
       (Unit. (clj/+ mg1 mg3) (or ms1 ms2))))
  ([u1 u2 & more]
     (reduce + (+ u1 u2) more)))


(defn ^Unit *
  "Return the product of units. The leftmost multiplicand with a
  non-nil unit of measurement determines the resulting unit's
  measurement value. If none of the multiplicands have a measurement
  value the resulting unit will be without measurement. 
  
  Example:

    (*)
    => #garden.units.Unit{:magnitude 1, :measurement nil}

    (* 1)
    => #garden.units.Unit{:magnitude 1, :measurement nil}

    (* 1 (px 5))
    => #garden.units.Unit{:magnitude 5, :measurement :px}

    (* (cm 5) (in 5) (mm 5))
    => #garden.units.Unit{:magnitude 31.75, :measurement :cm}"
  ([]
     (unit 1))
  ([u]
     (unit u))
  ([u1 u2]
     (let [mg1 (magnitude u1)
           mg2 (magnitude u2)
           ms1 (or (measurement u1)
                   (measurement u2))
           ms2 (or (measurement u2)
                   (measurement u1))
           mg3 (convert mg2 ms2 ms1)]
       (Unit. (clj/* mg1 mg3) (or ms1 ms2))))
  ([u1 u2 & more]
     (reduce * (* u1 u2) more)))


(defn ^Unit -
  "Return the difference of units. The leftmost minuend or subtrahend
  with a non-nil unit of measurement determines the resulting unit's
  measurement value. If neither minuend or subtrahends have a
  measurement value the resulting unit will be without measurement.


  Example:

    (- 1)
    => #garden.units.Unit{:magnitude -1, :measurement nil}

    (- (px 1))
    => #garden.units.Unit{:magnitude -1, :measurement :px}

    (- 1 (px 5))
    => #garden.units.Unit{:magnitude -4, :measurement :px}

    (- (cm 5) (in 5) (mm 5))
    => #garden.units.Unit{:magnitude -8.2, :measurement :cm}"
  ([u]
     (update-in (unit u) [:magnitude] clj/-))
  ([u1 u2]
     (let [mg1 (magnitude u1)
           mg2 (magnitude u2)
           ms1 (or (measurement u1)
                   (measurement u2))
           ms2 (or (measurement u2)
                   (measurement u1))
           mg3 (convert mg2 ms2 ms1)]
       (Unit. (clj/- mg1 mg3) (or ms1 ms2))))
  ([u1 u2 & more]
     (reduce - (- u1 u2) more)))


(defn ^Unit /
  "Return the quotient of units. The leftmost dividend or divisor with
  a non-nil unit of measurement determines the resulting unit's
  measurement value. If neither dividend or divisors have a
  measurement value the resulting unit will be without measurement.

  Example:

    (/ 1)
    => #garden.units.Unit{:magnitude 1, :measurement nil}

    (/ (px 1))
    => #garden.units.Unit{:magnitude 1, :measurement :px}

    (/ 1 (px 5))
    => #garden.units.Unit{:magnitude 1/5, :measurement :px}

    (/ (cm 5) (in 5) (mm 5)) 
    => #garden.units.Unit{:magnitude 0.7874015748031497, :measurement :cm}"
  ([u]
     (update-in (unit u) [:magnitude] clj//))
  ([u1 u2]
     (let [mg1 (magnitude u1)
           mg2 (magnitude u2)
           ms1 (or (measurement u1)
                   (measurement u2))
           ms2 (or (measurement u2)
                   (measurement u1))
           mg3 (convert mg2 ms2 ms1)]
       (Unit. (clj// mg1 mg3) (or ms1 ms2))))
  ([u1 u2 & more]
     (reduce / (/ u1 u2) more)))


;; ---------------------------------------------------------------------
;; Predefined units

;;; Absolute units

(defunit cm)
(defunit mm)
(defunit in)
(defunit px)
(defunit pt)
(defunit pc)
(defunit percent "%")

;;; Font-relative units

(defunit em)
(defunit ex)
(defunit ch)
(defunit rem)

;;; Viewport-percentage lengths

(defunit vw)
(defunit vh)
(defunit vmin)
(defunit vmax)

;;; Angles

(defunit deg)
(defunit grad)
(defunit rad)
(defunit turn)

;;; Times

(defunit s)
(defunit ms)

;;; Frequencies

(defunit Hz)
(defunit kHz)

;;; Resolutions

(defunit dpi)
(defunit dpcm)
(defunit dppx)


;; ---------------------------------------------------------------------
;; Predefined conversions

;;; Absolute units

(add-conversion! :cm :mm 10)
(add-conversion! :cm :pc 2.36220473)
(add-conversion! :cm :pt 28.3464567)
(add-conversion! :cm :px 37.795275591)
(add-conversion! :in :cm 2.54)
(add-conversion! :in :mm 25.4)
(add-conversion! :in :pc 6)
(add-conversion! :in :pt 72)
(add-conversion! :in :px 96)
(add-conversion! :mm :pt 2.83464567)
(add-conversion! :mm :px 3.7795275591)
(add-conversion! :pc :mm 4.23333333)
(add-conversion! :pc :pt 12)
(add-conversion! :pc :px 16)
(add-conversion! :pt :px 1.3333333333)

;;; Angles

(add-conversion! :deg :grad 1.111111111)
(add-conversion! :deg :rad 0.0174532925)
(add-conversion! :deg :turn 0.002777778)
(add-conversion! :grad :rad 63.661977237)
(add-conversion! :grad :turn 0.0025)
(add-conversion! :rad :turn 0.159154943)

;;; Times

(add-conversion! :s :ms 1000)

;;; Frequencies

(add-conversion! :Hz :kHz 0.001)


;; ---------------------------------------------------------------------
;; Protocol implementation


;;; Long

(extend-type Long
  IUnit
  (-unit [this]
    (Unit. this nil))

  IMagnitude
  (-magnitude [this] this)

  IMeasurement
  (-measurement [this] nil))


;;; Double

(extend-type Double
  IUnit
  (-unit [this]
    (Unit. this nil))

  IMagnitude
  (-magnitude [this] this)

  IMeasurement
  (-measurement [this] nil))


;;; String

(def ^{:private true
       :doc "Regular expression for matching a CSS unit. The magnitude
  and unit are captured."}
  unit-re
  #"([+-]?\d+(?:\.?\d+)?)(p[xtc]|in|[cm]m|%|r?em|ex|ch|v(?:[wh]|m(?:in|ax))|deg|g?rad|turn|m?s|k?Hz|dp(?:i|cm|px))")

(defn ^Unit parse-unit [s]
  (let [s' (string/trim s)]
    (if-let [[_ ^String magnitude measurement] (re-matches unit-re s')]
      (let [magnitude (if (.contains magnitude ".")
                        (Double/parseDouble magnitude)
                        (Long/parseLong magnitude))]
        (Unit. magnitude measurement)))))


(extend-type String
  IUnit
  (-unit [this]
    (parse-unit this))

  IMagnitude
  (-magnitude [this]
    (.-magnitude ^Unit (parse-unit this)))

  IMeasurement
  (-measurement [this]
    (.-measurement ^Unit (parse-unit this))))


(extend-type clojure.lang.Keyword
  IUnit
  (-unit [this]
    (parse-unit (name this)))

  IMagnitude
  (-magnitude [this]
    (.-magnitude ^Unit (parse-unit (name this))))

  IMeasurement
  (-measurement [this]
    (.-measurement ^Unit (parse-unit (name this)))))
