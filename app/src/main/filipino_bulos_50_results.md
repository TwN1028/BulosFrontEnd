# Filipino-to-Bulos 50-Sentence Integration Results

- Input fixture: `evaluation/filipino_bulos_50.json` (50 sentences, original order).
- Dataset: `data/Dumagat_Dataset_2026-10-22_03-02.xlsx`; regenerated `dictionary.json` and `sentence.json`.
- Execution: FastAPI `/api/v1/translate/` route using the production `TranslationService`; source `tl`, target `bul`; device ID `123e4567-e89b-42d3-a456-426614174000`.
- OPUS-MT used: not applicable for `tl` to `bul`; this pipeline is local-only and made no external provider calls.
- Confidence is the pipeline score, not an accuracy measure. Unresolved terms are the final local matching pass results; zero-match inputs are reported as unresolved phrases.

## Summary

- Inputs: 50
- Returned HTTP 200 results: 50
- OPUS-MT used: 0 (not applicable for all 50 inputs)
- Exact verified full-sentence references: 25
- Results with unresolved source Filipino remaining in output: 17
- Failed requests/exceptions: 0

## Results

| # | Filipino input | Actual Bulos output | Method | Confidence | OPUS-MT used | Unresolved Filipino words/phrases | Status |
|---:|---|---|---|---:|---|---|---|
| 1 | Kumusta ka? | Magiano kamu di? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 2 | Magandang umaga po. | Masampat a abi-abi. abi-abi | fuzzy_match | 1.000 | not applicable | none | unverified output |
| 3 | Ako si Maria. | aku i Maria. | word_match | 1.000 | not applicable | none | unverified output |
| 4 | Ano ang pangalan mo? | Ano i ngalan mu? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 5 | Taga-saan ka? | Tage-deno ka? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 6 | Mabuti naman ako. | Masampat pan aku. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 7 | Salamat sa tulong mo. | Salamat de tabengan mo. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 8 | Walang anuman. | walang anuman. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 9 | Nasaan ang bahay mo? | Denu i beloy mo? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 10 | Nasa bahay ako. | De beloy aku. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 11 | Sino ang kasama mo? | inu i kasama mo? | fuzzy_match_partial | 0.750 | not applicable | kasama | partial mapping |
| 12 | Ito ang libro ko. | Io i aklat ko. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 13 | Akin ang bag na ito. | děku i bag na io. | fuzzy_match_partial | 0.800 | not applicable | na | partial mapping |
| 14 | May tubig ba kayo? | May orat ba ikamu? | fuzzy_match_partial | 0.500 | not applicable | May, ba | partial mapping |
| 15 | Wala akong pera ngayon. | Eyen aku waklat nono. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 16 | Gusto ko ng tubig. | buot ko ng orat. | fuzzy_match_partial | 0.500 | not applicable | ko ng | partial mapping |
| 17 | Gusto kong kumain. | buot kong mamangan. | fuzzy_match_partial | 0.667 | not applicable | kong | partial mapping |
| 18 | Gutom na ako. | Gutom na aku. | fuzzy_match_partial | 0.333 | not applicable | Gutom na | partial mapping |
| 19 | Kumain na kami. | Mamangan ikami. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 20 | Nagluluto si Nanay ng pagkain. | Nagluluto i inoy ng papangan. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 21 | Umiinom siya ng kape. | nag-inum eya ng kape. | fuzzy_match_partial | 0.500 | not applicable | ng kape | partial mapping |
| 22 | Uminom ka muna ng tubig. | nag-inum ka muna ng orat. | fuzzy_match_partial | 0.400 | not applicable | ka muna, ng | partial mapping |
| 23 | Huwag kang tumakbo. | uwet i gumikan. | fuzzy_match | 1.000 | not applicable | none | unverified output |
| 24 | Pakibuksan ang pinto. | Pakibuksan i selongan. | fuzzy_match_partial | 0.667 | not applicable | Pakibuksan | partial mapping |
| 25 | Pakihintay po sandali. | Pakihintay po sandali. | unresolved_phrase | 0.000 | not applicable | Pakihintay po sandali | unverified output |
| 26 | Saan tayo pupunta? | Dênu ikitam dumuman? | fuzzy_match | 1.000 | not applicable | none | unverified output |
| 27 | Pupunta ako sa paaralan bukas. | Dumuman aku de paadelan di abi-abi. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 28 | Pumunta sila sa palengke kahapon. | dumuman ide de palingke depun. | word_match | 1.000 | not applicable | none | unverified output |
| 29 | Nag-aaral ang bata sa bahay. | Nag-aaral i anak de beloy. | fuzzy_match_partial | 0.800 | not applicable | Nag-aaral | partial mapping |
| 30 | Nagtatrabaho ang tatay ko sa Maynila. | magtarabeho i itoy ko de Maynila. | fuzzy_match_partial | 0.833 | not applicable | ko | partial mapping |
| 31 | Masaya ang mga bata. | nagkasalig i mga anak. | fuzzy_match_partial | 0.750 | not applicable | mga | partial mapping |
| 32 | Malaki ang bahay nila. | lalaki i beloy de. | word_match | 1.000 | not applicable | none | unverified output |
| 33 | Maliit ang aso namin. | miuntik i asū mi. | word_match | 1.000 | not applicable | none | unverified output |
| 34 | Nasaan ang pinakamalapit na tindahan? | Denu i alani lani a hedep tindahan? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 35 | Magkano ito? | Pide i oyo? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 36 | Masyadong mahal ang damit na ito. | Masyadong mahal i alit a io. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 37 | Bumili ako ng dalawang mangga. | Bumili aku ng aduwa mangge. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 38 | Bigyan mo ako ng isang baso ng tubig. | Biyen mo aku ng isin baso ng orat. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 39 | Ibinigay niya sa akin ang libro. | Binoy na de děku i aklat. | word_match | 1.000 | not applicable | none | unverified output |
| 40 | Hindi ko alam ang sagot. | Eyen ko alam i sagot. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 41 | Hindi siya pumasok dahil may sakit siya. | eyen eya pumasok dahil may bekot eya. | fuzzy_match_partial | 0.571 | not applicable | pumasok dahil, may | partial mapping |
| 42 | Bakit ka umiiyak? | bekot ka umiiyak? | fuzzy_match_partial | 0.333 | not applicable | ka umiiyak | partial mapping |
| 43 | Kailan kayo uuwi? | kapide ikamu uuwi? | fuzzy_match_partial | 0.667 | not applicable | uuwi | partial mapping |
| 44 | Maaari ba akong sumama? | Maaari ba aku sumama? | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 45 | Hindi pa ako kumakain. | eyen pa aku mamangan. | fuzzy_match_partial | 0.750 | not applicable | pa | partial mapping |
| 46 | Naglinis kami ng bahay bago dumating ang bisita. | Maglenis ikami ng beloy bago dumatong i bisita. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 47 | Kapag umulan, mananatili kami sa bahay. | Kapag dumatong tapuk, mananatili ikami de beloy. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 48 | Gusto niyang matulog dahil pagod siya. | Buot na mapede dahil pagod eya. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 49 | Pakisabi kay Ana na naghihintay ako. | Pakisabi di Ana a naghihintay aku. | exact_phrase | 1.000 | not applicable | none | exact verified match |
| 50 | Kung may oras ka, tulungan mo ako. | Kung may odes ka, tabengan mo aku. | exact_phrase | 1.000 | not applicable | none | exact verified match |

## Exact dataset references

- Input #1: sentence.json entry 185: `Magiano kamu di?`.
- Input #4: sentence.json entry 64: `Ano i ngalan mu?`; sentence.json entry 65: `Anu i ngalan mo?`; sentence.json entry 66: `Anu i ngalan mu?`.
- Input #5: sentence.json entry 378: `Tage-deno ka?`.
- Input #6: sentence.json entry 208: `Masampat pan aku.`.
- Input #7: sentence.json entry 362: `Salamat de tabengan mo`.
- Input #8: sentence.json entry 389: `walang anuman`; sentence.json entry 390: `Walang anuman`.
- Input #9: sentence.json entry 272: `Denu i beloy mo?`.
- Input #10: sentence.json entry 269: `De beloy aku.`.
- Input #12: sentence.json entry 154: `Io i aklat ko.`.
- Input #15: sentence.json entry 388: `Eyen aku waklat nono.`.
- Input #19: sentence.json entry 182: `Mamangan ikami.`.
- Input #20: sentence.json entry 267: `Nagluluto i inoy ng papangan.`.
- Input #27: sentence.json entry 312: `Dumuman aku de paadelan di abi-abi.`.
- Input #34: sentence.json entry 274: `Denu i alani lani a hedep tindahan?`.
- Input #35: sentence.json entry 221: `Pide i oyo?`.
- Input #36: sentence.json entry 248: `Masyadong mahal i alit a io.`.
- Input #37: sentence.json entry 88: `Bumili aku ng aduwa mangge.`.
- Input #38: sentence.json entry 82: `Biyen mo aku ng isin baso ng orat.`.
- Input #40: sentence.json entry 126: `Eyen ko alam i sagot.`.
- Input #44: sentence.json entry 200: `Maaari ba aku sumama?`.
- Input #46: sentence.json entry 265: `Maglenis ikami ng beloy bago dumatong i bisita.`.
- Input #47: sentence.json entry 167: `Kapag dumatong tapuk, mananatili ikami de beloy.`.
- Input #48: sentence.json entry 117: `Buot na mapede dahil pagod eya.`.
- Input #49: sentence.json entry 291: `Pakisabi di Ana a naghihintay aku.`.
- Input #50: sentence.json entry 191: `Kung may odes ka, tabengan mo aku.`.

## Recurring unresolved Filipino words/phrases

- `may`: 2
- `kasama`: 1
- `na`: 1
- `ba`: 1
- `ko ng`: 1
- `kong`: 1
- `gutom na`: 1
- `ng kape`: 1
- `ka muna`: 1
- `ng`: 1
- `pakibuksan`: 1
- `pakihintay po sandali`: 1
- `nag-aaral`: 1
- `ko`: 1
- `mga`: 1
- `pumasok dahil`: 1
- `ka umiiyak`: 1
- `uuwi`: 1
- `pa`: 1

Unverified outputs are not claims of linguistic accuracy. Exact verified status means the output matches a full-sentence dataset reference; partial mapping records source content left unresolved alongside mapped content. Fuzzy outputs without exact sentence references remain unverified for translation quality. No translations were added from the evaluation outputs.
