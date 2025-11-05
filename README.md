# Factory Worker Check-in Service

A robust, event-driven attendance tracking system for factory workers that ensures reliable recording of clock-in/clock-out events while gracefully handling unreliable downstream systems through comprehensive failure recovery mechanisms.


## File Summary
- **clock** - Project Root Folder
- **architecture_diagram_siemens.drawio.png** - Architecture Diagram
- **siemens_energy_design_doc.docx** - Design solution
- **implementation_details_and_future_scope.docx** - Implementation details and future scope
- **working_screenshots_tests.docx** - Working app screenshots

## Running the Application

### Prerequisites

Make sure Kafka broker is running on `localhost:9092`

### Steps

1. Clone this repository
2. Navigate to the project directory and build:
```bash
   cd clock
   mvn clean package
```
3. Run the application:
```bash
   java -jar clock-1.0.jar
```
