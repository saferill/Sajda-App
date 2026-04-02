# Design System Specification: The Ethereal Sanctuary

## 1. Overview & Creative North Star
**The Creative North Star: "The Digital Maqam"**
Traditional Islamic architecture is defined by light, proportion, and the transition from the worldly to the spiritual. This design system rejects the "boxy" nature of standard mobile apps. Instead, it treats the screen as a *Maqam*—a sacred space. 

We achieve a high-end, editorial feel by moving away from rigid grids toward **intentional asymmetry and tonal depth**. The goal is a "quiet" UI that breathes. We use expansive white space (breathing room) and layered surfaces to guide the eye, ensuring the user feels a sense of tranquility (Sakinah) rather than cognitive load.

---

## 2. Colors & Surface Philosophy
Our palette is rooted in the "Soft Emerald" (#2D6A4F), but its application is nuanced. We do not use flat color; we use light and depth.

### The "No-Line" Rule
**Explicit Instruction:** 1px solid borders for sectioning are strictly prohibited. 
Boundaries must be defined solely through background color shifts. A `surface-container-low` section sitting on a `surface` background creates a natural, soft edge that feels premium and integrated.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers—like stacked sheets of fine handmade paper.
- **Base Layer:** `surface` (#F7FAF8)
- **Secondary Content:** `surface-container-low` (#F1F4F2)
- **Interactive Cards:** `surface-container-lowest` (#FFFFFF) to create a "lifted" effect.
- **Deep Dark Mode:** In dark mode, reverse this logic using `surface-dim` and `surface-variant` to maintain legibility without harsh contrast.

### The "Glass & Gradient" Rule
To avoid a "templated" look, floating elements (like a prayer time countdown) should use **Glassmorphism**:
- **Fill:** `surface-variant` at 60% opacity.
- **Effect:** 20px Backdrop Blur.
- **Signature Gradient:** Use a subtle linear gradient for primary CTAs: `primary-container` (#2D6A4F) to `primary` (#0F5238) at a 135-degree angle. This provides a visual "soul" that flat hex codes cannot achieve.

---

## 3. Typography: The Calligraphic Balance
The typography system is a dialogue between the timeless elegance of Arabic script and the functional clarity of modern Latin type.

*   **Arabic Script (Amiri):** Used for Quranic verses and Dhikr. It must be sized at least 20% larger than the English counterpart to maintain visual weight parity.
*   **Latin Script (Inter/Manrope):** High-end editorial feel using wide tracking on labels and tight leading on headlines.

| Token | Font | Size | Intent |
| :--- | :--- | :--- | :--- |
| **display-lg** | Manrope | 3.5rem | Large numeral prayer times or poetic headings. |
| **headline-md** | Manrope | 1.75rem | Section headers; bold, authoritative, yet airy. |
| **title-md** | Inter | 1.125rem | Card titles and primary navigation. |
| **body-lg** | Inter | 1.0rem | Standard reading text; optimized for legibility. |
| **label-md** | Inter | 0.75rem | Uppercase with 0.05em tracking for metadata. |

---

## 4. Elevation & Depth: Tonal Layering
We do not use structural lines. We use physics and light.

*   **The Layering Principle:** Depth is achieved by "stacking" surface-container tiers. Place a `surface-container-lowest` card on a `surface-container-low` background. This creates a soft, natural lift.
*   **Ambient Shadows:** For floating action buttons or high-priority modals, use a custom shadow: 
    *   *Shadow:* `0 12px 32px -4px rgba(15, 82, 56, 0.08)` (A tinted shadow using the Primary On-Surface color, never pure grey).
*   **The "Ghost Border" Fallback:** If a border is required for accessibility, use `outline-variant` at 15% opacity. Never use 100% opaque borders.

---

## 5. Signature Components

### The Sanctuary Card (Cards & Lists)
*   **Constraint:** Zero dividers. Use vertical spacing (`spacing-6` or `spacing-8`) to separate list items.
*   **Styling:** `radius-xl` (1.5rem), background `surface-container-lowest`.
*   **Interaction:** On press, the card should scale to 98% rather than changing color, mimicking the compression of a physical object.

### The Prayer CTA (Buttons)
*   **Primary:** `primary-container` background with `on-primary` text. `radius-full` for a soft, pill-shaped feel.
*   **Secondary:** `surface-container-high` background with `primary` text. No border.
*   **Tertiary:** Transparent background, `primary` text, `label-md` styling (all caps, tracked out).

### Spiritual Progress (Chips)
*   **Style:** `secondary-container` fill with `on-secondary-container` text. 
*   **Context:** Use for "Completed" Tasbih or "Current" Surah markers. Use `radius-sm` (0.25rem) to provide a slight architectural contrast to the rounded cards.

### Navigation (The Floating Dock)
Instead of a standard bottom nav bar, use a floating dock with a backdrop-blur. 
*   **Fill:** `surface` at 80% opacity.
*   **Blur:** 16px.
*   **Shadow:** Ambient primary-tinted shadow.

---

## 6. Do's and Don'ts

### Do:
*   **Do** use asymmetrical margins. For example, a header might be offset by `spacing-8` on the left but `spacing-12` on the right to create an editorial, non-symmetrical flow.
*   **Do** use `primary-fixed-dim` for inactive states in dark mode to keep the "Emerald" soul present even when dimmed.
*   **Do** allow the Arabic script to "own" the space. Give it more line-height than the Latin text.

### Don't:
*   **Don't** use pure black (#000000) for dark mode. Use `inverse-surface` to maintain a "soft paper" feel.
*   **Don't** use standard Material Design "Drop Shadows." They are too heavy for this spiritual context.
*   **Don't** use icons with sharp 90-degree corners. All iconography must have a 1.5pt rounded cap to match the `roundedness-scale`.

---

## 7. Spacing & Rhythm
We follow a 0.35rem base unit to create a custom rhythm that feels distinct from the standard 4px/8px grids.

*   **In-Card Padding:** `spacing-5` (1.7rem) for a luxurious, spacious feel.
*   **Section Gaps:** `spacing-10` (3.5rem) to ensure the user’s mind can "reset" between different types of content (e.g., between Prayer Times and Daily Verse).