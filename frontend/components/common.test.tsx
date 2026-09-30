import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ErrorText, Field, Initials, PageHeader, StatusBadge, humanize, initials } from "./common";

describe("humanize", () => {
  it("turns backend codes into words", () => {
    expect(humanize("WEBSITE_FORM")).toBe("Website form");
    expect(humanize("UNASSIGNED")).toBe("Unassigned");
    expect(humanize(null)).toBe("");
  });
});

describe("initials", () => {
  it("takes the first letters of the first two words", () => {
    expect(initials("natasha kapoor singh")).toBe("NK");
    expect(initials("  Anuj  ")).toBe("A");
    expect(initials(undefined)).toBe("?");
  });
});

describe("StatusBadge", () => {
  it("shows a humanised status with its colour", () => {
    render(<StatusBadge status="CLOSED" />);
    expect(screen.getByText("Closed")).toHaveClass("bg-zinc-100");
  });
  it("uses a neutral style for unknown statuses and custom text when given", () => {
    render(<StatusBadge status="SOMETHING_NEW">Custom</StatusBadge>);
    expect(screen.getByText("Custom")).toHaveClass("bg-muted");
  });
  it("renders nothing without a status", () => {
    const { container } = render(<StatusBadge />);
    expect(container).toBeEmptyDOMElement();
  });
});

describe("layout helpers", () => {
  it("PageHeader shows its title, description and actions", () => {
    render(<PageHeader eyebrow="Leads" title="All leads" description="Newest first" icon={<svg />} actions={<button>Add</button>} />);
    expect(screen.getByRole("heading", { name: "All leads" })).toBeInTheDocument();
    expect(screen.getByText("Newest first")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Add" })).toBeInTheDocument();
  });

  it("Field labels its control and ErrorText only renders with a message", () => {
    render(<Field label="Email" hint=" (optional)"><input /></Field>);
    expect(screen.getByText("(optional)")).toBeInTheDocument();
    const { container } = render(<ErrorText />);
    expect(container).toBeEmptyDOMElement();
    render(<ErrorText>Phone is required</ErrorText>);
    expect(screen.getByText("Phone is required")).toBeInTheDocument();
  });

  it("Initials shows the person's initials", () => {
    render(<Initials name="Indu Rani" />);
    expect(screen.getByText("IR")).toBeInTheDocument();
  });
});
