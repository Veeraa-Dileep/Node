# Design Document – Personal Fitness Tracking Android App

## 1. Design Inspiration Overview

### Source of Inspiration
The design is inspired by a modern, dark-mode fitness app UI featuring:
- Card-based layouts
- Strong typography hierarchy
- High-contrast dark theme
- Bottom navigation with central action
- Dashboard-first experience

While the reference image focuses on coaches and programs, this app adapts the same visual language for **personal fitness tracking**, removing social and promotional elements.

---

## 2. Design Principles

### Core Principles
- **Dark Mode First** – optimized for gym environments
- **Speed Over Decoration** – minimal visual noise
- **Content-Dominant UI** – data > imagery
- **One-Handed Use** – reachable controls
- **Glanceable Progress** – stats visible in <3 seconds

### What We Keep from the Inspiration
- Card layouts
- Rounded containers
- Subtle gradients for primary actions
- Icon + text combinations
- Bold numeric stats

### What We Remove
- Social proof (followers, coaches)
- Marketing CTAs
- Program upsells
- Overuse of imagery

---

## 3. Color System

### Primary Colors
- Background: `#0E0E0E` (near-black)
- Surface Cards: `#1A1A1A`
- Primary Accent: Gradient Orange (`#FF9F1C → #FFB703`)
- Secondary Accent: Muted Green (`#4CAF50`)
- Error: `#E53935`

### Text Colors
- Primary Text: `#FFFFFF`
- Secondary Text: `#B0B0B0`
- Disabled Text: `#6F6F6F`

---

## 4. Typography

### Font Style
- Sans-serif (Roboto / Inter)

### Hierarchy
- H1 (Key Stats): Large, Bold
- H2 (Section Titles): Medium, Semi-Bold
- Body: Regular
- Metadata: Small, Muted

Numbers (weights, reps, PRs) should visually dominate text.

---

## 5. Global Layout Structure

### Navigation
Bottom Navigation Bar (Material Design):
- Dashboard
- History
- **Log Workout (Primary Action – Center Button)**
- Progress
- Settings

The center button mirrors the "+" action from the inspiration image.

---

## 6. Dashboard Screen

### Purpose
Provide an immediate overview of recent activity, progress, and next actions.

### Layout
1. **Header**
   - Greeting (e.g., "Good Evening")
   - Current week selector

2. **Stats Summary Card**
   - Workouts this week
   - Total volume lifted
   - Total workout time

3. **Quick Filters**
   - Chest / Back / Legs / Arms
   - Mirrors category pills from the image

4. **Recent Activity Cards**
   - Last workout
   - Recent PRs
   - Upcoming reminders

---

## 7. Workout Logging Screen

### Layout
- Full-screen, distraction-free
- Sticky header with workout timer
- Exercise cards stacked vertically

### Exercise Card
- Exercise name
- Set rows:
  - Weight
  - Reps
- Inline PR indicator (icon + highlight)
- Expandable notes field

### Design Inspiration Mapping
- Card styling and spacing directly inspired by workout cards in the image
- Reduced imagery, replaced with data density

---

## 8. Workout History Screen

### Views
- List view (default)
- Calendar view (toggle)

### List View
- Date header
- Workout summary card
  - Exercises performed
  - Duration
  - Volume

### Calendar View
- Highlight workout days
- Tap day → workout detail

---

## 9. Progress Screen

Tabbed interface inspired by profile sections in the image.

### Tabs
- PRs
- Weight
- Measurements
- Photos

### Graph Design
- Dark background
- Thin accent-colored lines
- Minimal gridlines
- Focus on trends, not exact values

---

## 10. Progress Photos Screen

### Gallery View
- Chronological grid
- Rounded thumbnails
- Date overlay

### Compare Mode
- Side-by-side images
- Slider or tap-to-toggle
- Date labels clearly visible

Visual inspiration comes from the large hero imagery used in the reference UI, adapted for personal photos.

---

## 11. Settings Screen

### Sections
- Appearance
  - Dark mode (default)
- Reminders
  - Weekly weigh-in
  - Monthly measurements
  - Progress photos
- Data
  - Backup
  - Restore
  - Export

Simple list layout, no cards.

---

## 12. Component Library

### Cards
- Rounded corners (16dp)
- Elevated slightly from background
- Consistent padding

### Buttons
- Primary: Gradient accent
- Secondary: Outlined
- Destructive: Red text only

### Icons
- Line icons
- High contrast
- Minimal decoration

---

## 13. Accessibility Considerations

- Minimum 4.5:1 contrast
- Large tap targets
- Scalable font sizes
- Clear visual focus states

---

## 14. Design Do’s and Don’ts

### Do
- Emphasize numbers and trends
- Use whitespace generously
- Keep interactions predictable

### Don’t
- Add social features
- Hide key actions
- Over-animate transitions

---

## 15. Future Visual Enhancements

- Subtle haptic feedback on PRs
- Micro-animations for logging completion
- Optional light mode
- Custom accent color themes

---

## 16. Design Summary

This design borrows the **premium, modern fitness aesthetic** from the reference image while transforming it into a **personal, data-focused training tool**. The UI prioritizes clarity, speed, and long-term usability — aligning tightly with the PRD’s offline-first, distraction-free philosophy.

