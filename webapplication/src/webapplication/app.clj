(ns webapplication.app
  (:use (compojure handler
                   [core :only (GET POST defroutes)]))
  (:require [net.cgrand.enlive-html :as en]
            [ring.util.response :as response]
            [ring.adapter.jetty :as jetty]))

(defonce korisnici (atom {}))

(defonce ukupanBMI 0)

(defonce ukupnaVisina 0)

(defonce ukupnaTezina 0)

(defonce brojMerenihKorisnika 0)

(def razlikaMV)

(def tekst "")

(def text "")

(def prazanString "---------------")

(def kime "" "korisnice")

(def tekstVisina "Unesite Vrednosti Za Visinu" )

(def tekstTezina "Unesite Vrednosti Za Tezinu")

(def tekstBMI "Kliknite na Dugme Izracunaj!!! ")

(defn razlika 
  [a b]
  (if(= a b)
     (do (def razlikaMV "ista")
       (- a b))
     (if (< a b)
       (do (def razlikaMV "veca")
         (- b a))
       (do (def razlikaMV "manja")
         (- a b)))))

(defn provera 
  [ brojMerenihKorisnika ] 
  (if (= brojMerenihKorisnika 0)
    (inc brojMerenihKorisnika)
    brojMerenihKorisnika))

(defn reset
  [] 
  (def ukupanBMI 0)
  (def ukupnaVisina 0)
  (def ukupnaTezina 0)
  (def brojMerenihKorisnika 0))

(defn formula
  [request]
  (def visina (Double. (:visina (:params request))))
  (def tezina (Double. (:tezina (:params request))))
  (def rezultat  
         (* (/ tezina (*  visina visina ))
             10000.0))
  
   (format "%.6s" (str rezultat)))

(defn registar
  [request]
  (do (if (@korisnici (:kime (:params request)))
        (def text "Ne mozete koristiti ovo korisnicko ime")
        (do (if (or (= (:ime (:params request)) "")  (= (:prezime (:params request)) ""))
              (def text "Niste dobro uneli ime ili prezime")
              (do  (def text "Uspesno ste se registrovali!")
                   (def kime (:kime (:params request)))
                   (swap! korisnici assoc (:kime (:params request)) {(:ime (:params request)) (:prezime (:params request))}))))
          )
      (response/redirect "/registracija")))

(defn izracunaj
  [request]  
      (do (def tekstVisina (str  kime  " ,Vasa visina, je za "
                                (format "%.6s" (razlika (/ ukupnaVisina (provera brojMerenihKorisnika)) visina ))  
                                 " cm "  razlikaMV " od prosecne!"))  
          (def tekstTezina (str  kime  " ,Vasa tezina, je za "
                                 (format "%.6s" (razlika (/ ukupnaTezina (provera brojMerenihKorisnika)) tezina ))
                                 " kg "  razlikaMV " od prosecne!"))
          (def tekstBMI (str  kime  " ,Vas BMI, je za "
                             (format "%.6s"  (razlika (/ ukupanBMI (provera brojMerenihKorisnika)) rezultat )) 
                              " "  razlikaMV " od prosecnog!"))
          (def tekst (formula request))
          (def ukupanBMI (+ ukupanBMI (Double. tekst)))
          (def brojMerenihKorisnika (+ brojMerenihKorisnika 1))
          (def ukupnaVisina (+ ukupnaVisina visina))
          (def ukupnaTezina (+ ukupnaTezina tezina))
          (response/redirect "/bmi")))  

(en/deftemplate homepage 
  (en/xml-resource "homepage.html")
  [request]
  )

(en/deftemplate kontakt 
  (en/xml-resource "kontakt.html")
  [request]
  )

(en/deftemplate registracija 
  (en/xml-resource "registracija.html")
  [request]
 [:#id :h2] (en/content (str text)))

(en/deftemplate bmi 
  (en/xml-resource "bmi.html")
  [request]
  [:#id :h2] (en/content (str "Vas BMI, " kime " je : " tekst))
  [:#table :#id2]   (en/content (str prazanString))
  [:#table :#p2]   (en/content (str "Prosecna visina nasih korisnika je "))
  [:#table :#p3]   (en/content (str (format "%.6s" (str (/ ukupnaVisina (provera brojMerenihKorisnika) ) ))))
  [:#table :#p4]   (en/content (str "Prosecna tezina nasih korisnika je "))
  [:#table :#p5]   (en/content (str (format "%.6s" (str (/ ukupnaTezina (provera brojMerenihKorisnika) ) ))))
  [:#table :#p6]   (en/content (str "Prosecan BMI nasih korisnika je"))
  [:#table :#p7]   (en/content (str (format "%.6s" (str (/ ukupanBMI (provera brojMerenihKorisnika)) ))))
  [:#table :#pk2]  (en/content (str tekstVisina))  
  [:#table :#pk3]  (en/content (str tekstTezina))
  [:#table :#pk4]   (en/content (str tekstBMI)))
    
(defroutes app*
  (GET "/" request (homepage request))
  (GET "/kontakt" request (kontakt request))
  (GET "/registracija" request (registracija request))
  (GET "/registar" request (registar request))
  (GET "/bmi" request (bmi request))
  (GET "/izracunaj" request (izracunaj request)))

(def app (compojure.handler/site app*)) 

