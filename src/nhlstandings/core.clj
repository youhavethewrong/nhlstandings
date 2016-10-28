(ns nhlstandings.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:gen-class))

(def current-season "20162017")
(def stats-api-root-url "https://statsapi.web.nhl.com/")
(def stats-url (str  stats-api-root-url "api/v1/standings?expand=standings.team&season=" current-season))

(defn check-standings
  [url]
  (client/get url))

(defn find-records
  [data team-name]
  (map
   (fn [r]
     (let [teams (:teamRecords r)]
       (filter
        #(= (:name %) team-name)
        (map
         (fn [team]
           {:name (:teamName (:team team))
            :city (:city team)
            :record  (:leagueRecord team)
            :played  (:gamesPlayed team)
            :scored  (:goalsScored team)
            :against (:goalsAgainst team)
            :rank    (:leagueRank team)
            :updated (:lastUpdated team)})
         teams))))
     (:records data)))

(defn -main
  [& args]
  (if-not (first args)
    (println "Please supply a team name like 'Stars'")
    (let [body (:body (check-standings stats-url))
          data (json/read-str body :key-fn keyword)
          records (apply concat (find-records data (first args)))]
      (dorun
       (map
        (fn [{:keys [name city record played scored against rank updated]}]
          (println "The" name "have a current season record of"
                   (str (:wins record) " wins, " (:losses record) " losses, and " (:ot record) " overtime losses")
                   "out of" played "games played."
                   "\nThis season, they have scored" scored "goals and have had" against "scored against them."
                   "\nThis information was last updated at" (str updated ".")))
        records)))))
