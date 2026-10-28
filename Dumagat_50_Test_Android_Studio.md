# 50-Item Bilingual Translator Regression Test

## Purpose

Use this fresh set to test Filipino-to-Bulos Dumagat and
English-to-Bulos Dumagat translation in both the mobile app and backend
server.

-   Items 1--25 are Filipino.
-   Items 26--50 are English.
-   These prompts avoid the two earlier 50-item English test lists from
    this conversation.
-   They are curated from common everyday sentence patterns and online
    phrase/sentence resources, not a statistically random sample of an
    entire corpus.
-   These are held-out test inputs. Do not add them to the `DATA` sheet
    or training dataset.

## Test set

1.  Pakisara ang pinto.
2.  Nasaan ang susi ng kotse?
3.  Magkita tayo mamayang hapon.
4.  May dala akong pagkain para sa iyo.
5.  Sandali lang, babalikan kita.
6.  Huwag mong kalimutan ang payong mo.
7.  Pakilagay ito sa ibabaw ng mesa.
8.  Anong oras nagsasara ang tindahan?
9.  Medyo malayo ang bahay nila.
10. Pwede bang makiraan?
11. Nag-iwan siya ng mensahe para sa iyo.
12. Maaga akong nagising kaninang umaga.
13. Wala nang laman ang bote.
14. Pakibuksan ang bintana.
15. Nag-aaral ang mga bata sa silid.
16. Nasira ang charger ng telepono ko.
17. May pasok ba bukas?
18. Magdala ka ng ekstrang damit.
19. Hindi ko makita ang hinahanap ko.
20. Masarap ang niluto mong ulam.
21. Umuuwi kami bago dumilim.
22. Pakisabi sa kanya na nandito na ako.
23. Kailangan kong bumili ng gamot.
24. Maingay sa labas ng bahay.
25. Maghintay ka sa tapat ng paaralan.
26. Please keep your voice down.
27. The keys are inside the drawer.
28. We missed the last bus.
29. Could you check the address again?
30. Her phone battery is almost empty.
31. I left my umbrella at the office.
32. The meeting starts in half an hour.
33. Please save a copy of this file.
34. Someone is knocking at the back door.
35. The soup needs a little more salt.
36. He forgot to bring his lunch.
37. Can you move this chair closer?
38. The road is closed for repairs.
39. I will send the details tonight.
40. She has been waiting since noon.
41. Put the groceries on the kitchen counter.
42. Our appointment was moved to Friday.
43. Please charge the tablet before class.
44. The package arrived earlier than expected.
45. I need a quiet place to study.
46. Turn left at the next street.
47. Neither answer seems correct.
48. We ran out of clean towels.
49. Keep this receipt in case you need it.
50. Let me know when you reach home.

## How to run the test

1.  Run the same 50 inputs, in the same order, against the backend and
    Android app.
2.  Use the Filipino input path for items 1--25 and the English input
    path for items 26--50.
3.  Record exact outputs before correcting anything manually.
4.  Compare app output with backend output for each item.
5.  Label issues separately: API/request failure, empty output,
    unchanged source text, wrong lexical match, word-order/grammar
    issue, punctuation/capitalization issue, or app/backend mismatch.
6.  Have a knowledgeable Bulos Dumagat speaker review the translations.
    The source sentence alone does not establish the correct Bulos
    translation.
7.  Keep results separate from the training dataset so the test remains
    unseen during tuning.

## Suggested results table

  ---------------------------------------------------------------------------
            \# Source     Input      Backend   Android   Match?    Reviewer
               language              output    output              verdict /
                                                                   notes
  ------------ ---------- ---------- --------- --------- --------- ----------
             1 Filipino   Pakisara                                 
                          ang pinto.                               

            26 English    Please                                   
                          keep your                                
                          voice                                    
                          down.                                    
  ---------------------------------------------------------------------------

Add one row for every test item; the two rows above are examples of the
format.

## Instructions for Android Studio

Use this as a manual regression checklist for the Android app and
compare its results with the backend.

-   Run the existing app on its normal target device or emulator. Do not
    change translation logic before collecting baseline results.
-   Enter items 1--25 through the Filipino input path and items 26--50
    through the English input path.
-   Record the exact displayed output, including punctuation and
    capitalization. If copy/share is supported, verify copied text
    matches the display.
-   Run the same inputs against the backend using the project's
    documented endpoint and request format. Do not guess routes or
    payloads.
-   For mismatches, record the input, both outputs, device/app details,
    and relevant Logcat or server logs.
-   Check loading states and network errors separately from the 50
    scored prompts.
-   Do not add test prompts to the production dataset. Review failures
    before changing code or data.
-   Summarize successful requests, app/backend mismatches,
    translation-quality issues, crashes, and unresolved cases.
