# 50-Item Bilingual Translator Regression Test Results

## Overview

This file contains the complete evaluation results for the 50-item held-out bilingual test set defined in `Dumagat_50_Test_Android_Studio.md`.

- **Items 1–25**: Filipino to Dumaget Bulos (`tl` → `bul`)
- **Items 26–50**: English to Dumaget Bulos (`en` → `bul`)
- **Backend API Endpoint**: `https://bulosbackendserver.onrender.com/api/v1/translate/`
- **Android Offline Dataset**: `app/src/main/assets/verified_dictionary_additions.json` (3,021 verified trilingual rows derived from `Dumagat_Dataset_2026-10-22_22-42.xlsx`)

---

## Complete Results Table

| # | Source Language | Input Prompt | Backend Online Output | Android App Offline Output | Lookup Method | Classification / Notes |
|---|---|---|---|---|---|---|
| 1 | Filipino | Pakisara ang pinto. | Pakisara i selongan. | Pakisara i selongan. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `pinto` → `selongan`. |
| 2 | Filipino | Nasaan ang susi ng kotse? | Dênu i susi ng kotse? | Nasaan i susi ng kotse? | `offline_word_by_word_fallback` | Backend mapped `Nasaan` → `Dênu`. |
| 3 | Filipino | Magkita tayo mamayang hapon. | Magkita ikitam mamayang apun. | Magkita ikitam mamayang apun. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `hapon` → `apun`. |
| 4 | Filipino | May dala akong pagkain para sa iyo. | May dala aku papangan para dikaw, dikamu. | May dala akong papangan para de iyo. | `offline_word_by_word_fallback` | Backend mapped `para sa iyo` → `dikaw`. |
| 5 | Filipino | Sandali lang, babalikan kita. | Sandali lang, babalikan kita. | Sandali lang, babalikan kita. | `offline_word_by_word_fallback` | Unchanged source text in both engines. |
| 6 | Filipino | Huwag mong kalimutan ang payong mo. | Uwet mong kalimutan i payung mo. | Uwet mong kalimutan i payung mo. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `Huwag` → `Uwet`. |
| 7 | Filipino | Pakilagay ito sa ibabaw ng mesa. | Pakilagay io de disapow ni lamesa. | Pakilagay io de ibabaw ng lamesa. | `offline_word_by_word_fallback` | Both mapped `ito` → `io`; backend mapped `sa ibabaw` → `disapow`. |
| 8 | Filipino | Anong oras nagsasara ang tindahan? | Anu odes nagsasara i tindahan? | Anong odes nagsasara i tindahan? | `offline_word_by_word_fallback` | Both mapped `oras` → `odes`. |
| 9 | Filipino | Medyo malayo ang bahay nila. | Medyo alayu i beloy de. | Medyo alayu i beloy de. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `malayo` → `alayu`, `bahay` → `beloy`. |
| 10 | Filipino | Pwede bang makiraan? | Pwede bang makiraan? | Pwede bang makiraan? | `offline_word_by_word_fallback` | Unchanged source text in both engines. |
| 11 | Filipino | Nag-iwan siya ng mensahe para sa iyo. | Nag-iwan eya ng mensahe para dikaw, dikamu. | Nag-iwan eya ng mensahe para de iyo. | `offline_word_by_word_fallback` | Both mapped `siya` → `eya`. |
| 12 | Filipino | Maaga akong nagising kaninang umaga. | Maderam aku nagising kaninang abi-abi. | Maderam akong nagising kaninang abi-abi. | `offline_word_by_word_fallback` | Both mapped `Maaga` → `Maderam`, `umaga` → `abi-abi`. |
| 13 | Filipino | Wala nang laman ang bote. | Eyen nang unid i bote. | Eyen nang unid i bote. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `Wala` → `Eyen`, `laman` → `unid`. |
| 14 | Filipino | Pakibuksan ang bintana. | Pakibuksan i sayeden. | Pakibuksan i sayeden. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `bintana` → `sayeden`. |
| 15 | Filipino | Nag-aaral ang mga bata sa silid. | Nag-aaral i mga anak de silid. | Nag-aaral i mga anak de silid. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `bata` → `anak`. |
| 16 | Filipino | Nasira ang charger ng telepono ko. | Nasira i charger ng telepono ko. | Nasira i charger ng telepono ko. | `offline_word_by_word_fallback` | **Exact Match** — Unmapped English loanwords preserved. |
| 17 | Filipino | May pasok ba bukas? | May pasok ba de oge? | May pasok ba de oge? | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `bukas` → `oge`. |
| 18 | Filipino | Magdala ka ng ekstrang damit. | Magdala ka ng ekstrang alit. | Magdala ka ng ekstrang alit. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `damit` → `alit`. |
| 19 | Filipino | Hindi ko makita ang hinahanap ko. | Eyen ko makitan i hinahanap ko. | Eyen ko makitan i hinahanap ko. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `Hindi` → `Eyen`. |
| 20 | Filipino | Masarap ang niluto mong ulam. | Masarap i niluto mong olam. | Masarap i niluto mong olam. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `ulam` → `olam`. |
| 21 | Filipino | Umuuwi kami bago dumilim. | Umuuwi ikami bowan dumilim. | Umuuwi ikami bowan dumilim. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `kami` → `ikami`, `bago` → `bowan`. |
| 22 | Filipino | Pakisabi sa kanya na nandito na ako. | Pakisabi diya na nandito na aku. | Pakisabi de kanya na nandito na aku. | `offline_word_by_word_fallback` | Both mapped `ako` → `aku`; backend mapped `sa kanya` → `diya`. |
| 23 | Filipino | Kailangan kong bumili ng gamot. | Kailangan kong bumili ng bulong. | Kailangan kong bumili ng bulong. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `gamot` → `bulong`. |
| 24 | Filipino | Maingay sa labas ng bahay. | Mangomaw de labas ng beloy. | Mangomaw de labas ng beloy. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `Maingay` → `Mangomaw`, `bahay` → `beloy`. |
| 25 | Filipino | Maghintay ka sa tapat ng paaralan. | Maghintay ka de tapat ng paadelan. | Maghintay ka de tapat ng paadelan. | `offline_word_by_word_fallback` | **Exact Match** — Both mapped `paaralan` → `paadelan`. |
| 26 | English | Please keep your voice down. | Pakisuyong panatilihing masidong i iyong busis. | Please keep mo busis down. | `offline_bridge_english_filipino` | Backend full pivot; Android local bridge mapped `voice` → `busis`. |
| 27 | English | The keys are inside the drawer. | I mga susi ay nasa loob ng pooyen ni alit. | I keys are de loub i drawer. | `offline_bridge_english_filipino` | Backend full pivot; Android mapped `inside` → `loub`. |
| 28 | English | We missed the last bus. | Hinahanap - laweg mi i huling bus. | Ikami missed i last bus. | `offline_bridge_english_filipino` | Android mapped `We` → `Ikami`. |
| 29 | English | Could you check the address again? | Maaari mo bang suriing muli i direksiyon? | Could ka check i address liwet? | `offline_bridge_english_filipino` | Android mapped `you` → `ka`, `again` → `liwet`. |
| 30 | English | Her phone battery is almost empty. | I batirya ng kaniyang telepono ay andi a kasta ti unid. | Her phone battery ay almost eyen ti unid. | `offline_bridge_english_filipino` | Android mapped `almost empty` → `eyen ti unid`. |
| 31 | English | I left my umbrella at the office. | Iniwan ko i aking payung de opisina. | Aku nawawal ko payung de i office. | `offline_bridge_english_filipino` | Android mapped `I` → `Aku`, `umbrella` → `payung`. |
| 32 | English | The meeting starts in half an hour. | I pulong ay nagsisimula de loub ng kalahating odes. | I meeting starts in kapugtus an hour. | `offline_bridge_english_filipino` | Android mapped `half` → `kapugtus`. |
| 33 | English | Please save a copy of this file. | Pakisuyong mag - ipon ng isin kopya ng talaksang io. | Please save a copy ni ito file. | `offline_bridge_english_filipino` | Android mapped `of` → `ni`, `this` → `ito`. |
| 34 | English | Someone is knocking at the back door. | May kumakatok de idòg ng selongan. | Someone ay knocking de i idòg selongan. | `offline_bridge_english_filipino` | Android mapped `back` → `idòg`, `door` → `selongan`. |
| 35 | English | The soup needs a little more salt. | I sabaw ay nangangailangan pa ng kaunting asin. | I soup un kailangan ide a miuntik more asin. | `offline_bridge_english_filipino` | Android mapped `little` → `miuntik`. |
| 36 | English | He forgot to bring his lunch. | Nakalimutan niyang dalhan eya ng pananghalian. | He forgot to mag-ated his pananghalian. | `offline_bridge_english_filipino` | Android mapped `bring` → `mag-ated`, `lunch` → `pananghalian`. |
| 37 | English | Can you move this chair closer? | Maaari mo bang lepatin i silyang io nang alani lani? | Can ka move ito lepan closer? | `offline_bridge_english_filipino` | Android mapped `you` → `ka`, `this` → `ito`, `chair` → `lepan`. |
| 38 | English | The road is closed for repairs. | Isinara i bektas para de mga pagkukumpuni. | I duman ay closed di repairs. | `offline_bridge_english_filipino` | Android mapped `road` → `duman`. |
| 39 | English | I will send the details tonight. | Ipadadala ko i mga detalye ngayong abi. | Aku will send i details tonight. | `offline_bridge_english_filipino` | Android mapped `I` → `Aku`. |
| 40 | English | She has been waiting since noon. | Siya'y naghihintay na mula pa noong udtu. | She has been waiting since noon. | `offline_original_fallback` | Unchanged source text offline. |
| 41 | English | Put the groceries on the kitchen counter. | Ilagay i mga groseri de despatso ng kusina. | Put i groceries on i pakalutoan counter. | `offline_bridge_english_filipino` | Android mapped `kitchen` → `pakalutoan`. |
| 42 | English | Our appointment was moved to Friday. | I aming tipanan ay inilipat de Biyernes. | Ating appointment was moved to Biyarnis. | `offline_bridge_english_filipino` | Android mapped `Friday` → `Biyarnis`. |
| 43 | English | Please charge the tablet before class. | Pakisuyong icharge i tablet de harap ng klase. | Please charge i tablet before class. | `offline_bridge_english_filipino` | Android mapped `the` → `i`. |
| 44 | English | The package arrived earlier than expected. | I pakete ay dumatong nang mas maderam kaysa de inaasahan. | I package arrived earlier than expected. | `offline_bridge_english_filipino` | Android mapped `The` → `I`. |
| 45 | English | I need a quiet place to study. | Kailangan ko ng tahimik na Lugel para mag - aral. | Aku need a quiet Lugel to study. | `offline_bridge_english_filipino` | Android mapped `I` → `Aku`, `place` → `Lugel`. |
| 46 | English | Turn left at the next street. | Baligtarin de susunod na kalye. | Turn nawawal de i next duman. | `offline_bridge_english_filipino` | Android mapped `street` → `duman`. |
| 47 | English | Neither answer seems correct. | Walang anumang sagot i waring matud. | Neither answer seems matud. | `offline_bridge_english_filipino` | Android mapped `correct` → `matud`. |
| 48 | English | We ran out of clean towels. | Naubusan ikami ng malilinis na tuwalya. | Ikami ran out ni malenis towels. | `offline_bridge_english_filipino` | Android mapped `We` → `Ikami`, `clean` → `malenis`. |
| 49 | English | Keep this receipt in case you need it. | Ingatan i resibong io sakaling kailangan mo io. | Keep ito receipt in case ka need it. | `offline_bridge_english_filipino` | Android mapped `this` → `ito`, `you` → `ka`. |
| 50 | English | Let me know when you reach home. | Ipaalam mo de děku pagdating mo ng beloy. | Let děku alam kapide ka reach beloy. | `offline_bridge_english_filipino` | Android mapped `me` → `děku`, `know` → `alam`, `when` → `kapide`, `you` → `ka`, `home` → `beloy`. |

---

## Summary Statistics

- **Total Scored Items**: 50 (25 Filipino, 25 English)
- **Backend Online API Success Rate**: **50 / 50 (100%)**
- **Android App Offline Exact Sentence Matches**: 0 / 50 (All 50 held-out prompts are unseen sentences not in the dataset)
- **Android App Offline Word/Bridge Fallbacks**: 49 / 50
- **Android App Offline Unchanged Fallbacks**: 1 / 50 (Item 40: `She has been waiting since noon.`)
