import type { InspectionSection } from '../model/Inspection';

export const FactsPanel = ({ sections }: { sections: InspectionSection[] }) => (
  <div className="admin-facts">
    {sections.map((section) => (
      <section key={section.key} className="admin-fact">
        <h3 className="admin-fact__label">{section.label}</h3>
        <pre className="admin-fact__value">{section.value}</pre>
      </section>
    ))}
  </div>
);
