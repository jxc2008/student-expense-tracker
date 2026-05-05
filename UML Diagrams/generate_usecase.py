"""Regenerate ExpenseTracker_UseCase.pdf with diagram (page 1) + use case table (page 2)."""

from pathlib import Path
import matplotlib.pyplot as plt
from matplotlib.patches import Ellipse, FancyArrowPatch, Rectangle
from matplotlib.backends.backend_pdf import PdfPages

OUT = Path(__file__).parent / "ExpenseTracker_UseCase.pdf"

# ---------- page 1: diagram ----------

def draw_diagram(ax):
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 10)
    ax.set_aspect("equal")
    ax.axis("off")
    ax.set_title("Personal Expense Tracker  —  Use-Case Diagram",
                 fontsize=16, fontweight="bold", pad=18)

    # System boundary
    ax.add_patch(Rectangle((3.0, 0.6), 10.6, 8.6, fill=False, lw=1.4))
    ax.text(8.3, 8.95, "Personal Expense Tracker", fontsize=11,
            style="italic", ha="center")

    # Actor: stick figure
    ax.add_patch(plt.Circle((1.3, 5.6), 0.22, fill=False, lw=1.4))
    ax.plot([1.3, 1.3], [5.38, 4.4], "k-", lw=1.4)        # body
    ax.plot([0.75, 1.85], [5.0, 5.0], "k-", lw=1.4)       # arms
    ax.plot([1.3, 0.85], [4.4, 3.7], "k-", lw=1.4)        # left leg
    ax.plot([1.3, 1.75], [4.4, 3.7], "k-", lw=1.4)        # right leg
    ax.text(1.3, 3.35, "User", fontsize=11, fontweight="bold", ha="center")

    # Use cases (id, label, x, y)
    UCS = {
        "UC-01": ("Add Expense",                5.6, 8.0),
        "UC-02": ("Edit Expense",               5.6, 6.8),
        "UC-03": ("Delete Expense",             5.6, 5.6),
        "UC-04": ("View All Expenses",          5.6, 4.4),
        "UC-05": ("Filter Expenses",            5.6, 3.0),
        "UC-06": ("View Monthly Summary",       5.6, 1.5),
        "UC-07": ("Select Category",           11.0, 7.4),
        "UC-08": ("Calculate Category Breakdown", 11.0, 1.5),
    }

    centers = {}
    for uc, (label, x, y) in UCS.items():
        e = Ellipse((x, y), 2.4, 0.95, facecolor="#e8eef9",
                    edgecolor="#2b3a55", lw=1.2)
        ax.add_patch(e)
        ax.text(x, y + 0.13, uc, fontsize=10, ha="center")
        ax.text(x, y - 0.18, label, fontsize=9.5, ha="center")
        centers[uc] = (x, y)

    # Actor → use case associations
    for uc in ("UC-01", "UC-02", "UC-03", "UC-04", "UC-05", "UC-06"):
        x, y = centers[uc]
        ax.plot([1.55, x - 1.22], [5.0, y], "k-", lw=1.0)

    # «include» / «extend» dashed arrows
    def dashed(p1, p2, label):
        arr = FancyArrowPatch(p1, p2, arrowstyle="->",
                              linestyle="--", color="black",
                              mutation_scale=14, lw=1.0)
        ax.add_patch(arr)
        mx, my = (p1[0] + p2[0]) / 2, (p1[1] + p2[1]) / 2
        ax.text(mx, my + 0.2, label, fontsize=9, style="italic", ha="center")

    dashed((centers["UC-01"][0] + 1.22, centers["UC-01"][1]),
           (centers["UC-07"][0] - 1.22, centers["UC-07"][1]), "«include»")
    dashed((centers["UC-02"][0] + 1.22, centers["UC-02"][1]),
           (centers["UC-07"][0] - 1.22, centers["UC-07"][1] - 0.35), "«include»")
    dashed((centers["UC-05"][0], centers["UC-05"][1] + 0.48),
           (centers["UC-04"][0], centers["UC-04"][1] - 0.48), "«extend»")
    dashed((centers["UC-06"][0] + 1.22, centers["UC-06"][1]),
           (centers["UC-08"][0] - 1.22, centers["UC-08"][1]), "«include»")

    # Legend
    ax.text(0.2, 2.6, "Legend", fontsize=10, fontweight="bold")
    ax.plot([0.2, 1.6], [2.2, 2.2], "k-", lw=1.0)
    ax.text(1.75, 2.18, "association", fontsize=9, va="center")
    ax.add_patch(FancyArrowPatch((0.2, 1.8), (1.6, 1.8), arrowstyle="->",
                                  linestyle="--", color="black",
                                  mutation_scale=12, lw=1.0))
    ax.text(1.75, 1.78, "«include» / «extend»", fontsize=9, va="center")


# ---------- page 2: use case table ----------

USE_CASES = [
    ["UC-01", "Add Expense",
     "User",
     "Record a new expense with amount, date, description, category.",
     "Application is running; categories list available.",
     "1. User opens 'Add Expense' form.\n"
     "2. User selects a category (UC-07).\n"
     "3. User enters amount, date, description.\n"
     "4. System validates input (non-null, amount ≥ 0).\n"
     "5. System stores the expense.",
     "Expense is saved and visible in the expense list."],

    ["UC-02", "Edit Expense",
     "User",
     "Modify the fields of an existing expense.",
     "At least one expense exists; user has selected one.",
     "1. User selects expense and chooses 'Edit'.\n"
     "2. Form pre-fills with current values.\n"
     "3. User changes fields; may reselect category (UC-07).\n"
     "4. System validates and replaces the stored expense.",
     "Selected expense is updated in place."],

    ["UC-03", "Delete Expense",
     "User",
     "Remove an existing expense from the tracker.",
     "At least one expense exists; user has selected one.",
     "1. User selects expense and chooses 'Delete'.\n"
     "2. System removes the expense from storage.\n"
     "3. List view refreshes.",
     "Expense is no longer present."],

    ["UC-04", "View All Expenses",
     "User",
     "Display every recorded expense in a list.",
     "Application is running.",
     "1. User opens the main expenses view.\n"
     "2. System fetches all expenses.\n"
     "3. System renders them in chronological / insertion order.",
     "User sees the full expense list (empty list if none)."],

    ["UC-05", "Filter Expenses",
     "User",
     "Narrow the expense list by category or date. Extends UC-04.",
     "User is on the 'View All Expenses' screen.",
     "1. User selects a filter (category or date).\n"
     "2. System computes the matching subset.\n"
     "3. List view shows only the filtered results.",
     "Only matching expenses are displayed; clearing the filter restores UC-04 view."],

    ["UC-06", "View Monthly Summary",
     "User",
     "See total spending grouped by category for a given month/year.",
     "Application is running; one or more expenses exist.",
     "1. User selects month and year.\n"
     "2. System runs UC-08 to compute totals per category.\n"
     "3. System displays the breakdown.",
     "Per-category totals for the selected month are shown."],

    ["UC-07", "Select Category",
     "User",
     "Choose a category for an expense being added or edited. Included by UC-01 and UC-02.",
     "Add or Edit Expense flow is in progress.",
     "1. System presents available categories.\n"
     "2. User picks one.\n"
     "3. System validates the choice and returns control to the calling use case.",
     "A valid category is attached to the expense in flight."],

    ["UC-08", "Calculate Category Breakdown",
     "System",
     "Aggregate expenses by category over a month/year. Included by UC-06.",
     "Triggered from UC-06 with a month and year.",
     "1. System filters expenses to the given month/year.\n"
     "2. System groups them by category name.\n"
     "3. System sums amounts per category and returns the map.",
     "A category → total-spend map is returned to UC-06."],
]


def draw_table(fig):
    """Draw the use case table manually with proper word wrapping."""
    import textwrap

    fig.suptitle("Personal Expense Tracker  —  Use-Case Specification Table",
                 fontsize=14, fontweight="bold", y=0.985)

    ax = fig.add_subplot(111)
    ax.set_xlim(0, 1)
    ax.set_ylim(0, 1)
    ax.axis("off")

    headers = ["ID", "Name", "Actor", "Description", "Preconditions",
               "Main Flow", "Postconditions"]
    # Column widths (fractions, sum = 1.0) and approx chars per column for wrapping
    col_w   = [0.05, 0.10, 0.06, 0.17, 0.16, 0.28, 0.18]
    col_chars = [6,    14,  7,    26,    24,    44,   28]

    x_edges = [0.0]
    for w in col_w:
        x_edges.append(x_edges[-1] + w)

    def wrap_cell(text, n):
        """Wrap each pre-existing line independently so '\n' bullets stay separate."""
        out = []
        for line in text.split("\n"):
            if not line:
                out.append("")
                continue
            out.extend(textwrap.wrap(line, width=n) or [""])
        return "\n".join(out)

    # Wrap content and compute per-row line counts
    wrapped_rows = []
    line_counts = []
    for row in USE_CASES:
        wrapped = [wrap_cell(str(cell), col_chars[i]) for i, cell in enumerate(row)]
        wrapped_rows.append(wrapped)
        line_counts.append(max(c.count("\n") + 1 for c in wrapped))

    # Layout: header height + per-row heights based on line counts
    line_h = 0.018
    pad_v  = 0.012
    header_h = 0.035
    row_heights = [line_h * lc + pad_v * 2 for lc in line_counts]
    total_h = header_h + sum(row_heights)

    top = 0.93
    if total_h > top - 0.03:
        # scale down to fit
        scale = (top - 0.03) / total_h
        line_h *= scale
        pad_v  *= scale
        header_h *= scale
        row_heights = [line_h * lc + pad_v * 2 for lc in line_counts]

    # Header row
    y = top
    for c, h in enumerate(headers):
        x0, x1 = x_edges[c], x_edges[c + 1]
        ax.add_patch(Rectangle((x0, y - header_h), x1 - x0, header_h,
                               facecolor="#2b3a55", edgecolor="white", lw=0.6))
        ax.text((x0 + x1) / 2, y - header_h / 2, h,
                color="white", fontweight="bold", fontsize=8.5,
                ha="center", va="center")
    y -= header_h

    # Body rows
    for r, row in enumerate(wrapped_rows):
        rh = row_heights[r]
        bg = "#f4f6fb" if r % 2 == 1 else "white"
        for c, text in enumerate(row):
            x0, x1 = x_edges[c], x_edges[c + 1]
            ax.add_patch(Rectangle((x0, y - rh), x1 - x0, rh,
                                   facecolor=bg, edgecolor="#cdd3df", lw=0.5))
            ax.text(x0 + 0.004, y - pad_v, text,
                    fontsize=7.2, ha="left", va="top",
                    family="DejaVu Sans")
        y -= rh


# ---------- write PDF ----------

with PdfPages(OUT) as pdf:
    fig1, ax1 = plt.subplots(figsize=(13, 9))
    draw_diagram(ax1)
    pdf.savefig(fig1, bbox_inches="tight")
    plt.close(fig1)

    fig2 = plt.figure(figsize=(16.5, 10.5))   # landscape, wider for the table
    draw_table(fig2)
    pdf.savefig(fig2, bbox_inches="tight")
    plt.close(fig2)

print(f"wrote {OUT}")
