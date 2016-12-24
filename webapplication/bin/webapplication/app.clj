(ns webapplication.app
  (:use (compojure handler
                   [core :only (GET POST defroutes)]))
  (:require [net.cgrand.enlive-html :as en]
            [ring.util.response :as response]
            [ring.adapter.jetty :as jetty]))

(defonce korisnici (atom {}))

(defn formula
  [tezina visina]
  (def resenje (/ tezina (/ (* visina visina) 10000.0)))
   resenje)

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
      (do (def tekst (formula (:visina (:params request)) (:tezina (:params request)))) 
	              #_ (:visina (:params request)) STRING VREDNOST ZBOG TOGA NE MOZE DA IZVRSI FUNCIJA FORMULA 
	      				             I DOLAZI DO GRESKE, PO MOM MISLJENJU. KAKO DA IZVRSIM KONVERZIJU STRINGA U 
	      					     DOUBLE?
		      (response/redirect "/bmi")))

(en/deftemplate homepage 
  (en/xml-resource "homepage.html")
  [request]
  )

(en/deftemplate bmi 
  (en/xml-resource "bmi.html")
  [request]
  [:#id :h2] (en/content (str "Vas BMI, " kime " je : " tekst) ))

(en/deftemplate kontakt 
  (en/xml-resource "kontakt.html")
  [request]
  )

(def tekst "")

(def text "")

(def kime "" "korisnice")

(en/deftemplate registracija 
  (en/xml-resource "registracija.html")
  [request]
 [:#id :h2] (en/content (str text)))
    
(defroutes app*
  (GET "/" request (homepage request))
  (GET "/bmi" request (bmi request))
  (GET "/kontakt" request (kontakt request))
  (GET "/registracija" request (registracija request))
  (GET "/registar" request (registar request))
  (GET "/izracunaj" request (izracunaj request)))

(def app (compojure.handler/site app*)) 
