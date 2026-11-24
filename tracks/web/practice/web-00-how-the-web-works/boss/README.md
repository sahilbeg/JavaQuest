# INC-00: The ₹ sign shows as garbage on Raju's phone

Fix the price board, `src/index.html`, in three tiers.

1. **Bronze:** add `<meta charset="utf-8">` inside `<head>`. Keep every ₹ price exactly as it is.
2. **Silver:** make that charset line the very first element inside `<head>`, before the `<title>` and the `<style>`, so it sits within the first 1024 bytes of the file.
3. **Gold:** give `<html>` the attribute `lang="en"`, and add `<meta name="viewport" content="width=device-width, initial-scale=1">` inside `<head>`.

Your own browser may show the ₹ correctly even before the fix, because it guesses well. The checks read the file itself. Press **Run checks** to see which tiers pass.
