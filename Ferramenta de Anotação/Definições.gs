//gets all the ontologies from BioPortal
function getBioPortalOntologies() {

    var searchString = "http://data.bioontology.org/ontologies?apikey=df3b13de-1ff4-4396-a183-80cc845046cb&display_links=false&display_context=false";

    // we cache results and try to retrieve them on every new execution.
    var cache = CacheService.getPrivateCache();

    var text;


    text = UrlFetchApp.fetch(searchString).getContentText();
    splitResultAndCache(cache, "ontologies", text);
    
    var doc = JSON.parse(text);
    var ontologies = doc;

    var ontologyDictionary = {};
    for (ontologyIndex in doc) {
        var ontology = doc[ontologyIndex];
      ontologyDictionary[ontology.acronym] = {"name":ontology.name, "uri":ontology["@id"]};
    }

    return sortOnKeys(ontologyDictionary);

}


function getAgroPortalOntologies() {

  var searchString = "http://data.agroportal.lirmm.fr/ontologies?apikey=753de730-46c4-431d-ad71-5b468a91a5ef&display_links=false&display_context=false";

    // we cache results and try to retrieve them on every new execution.
    var cache = CacheService.getPrivateCache();

    var text;

    text = UrlFetchApp.fetch(searchString).getContentText();
    splitResultAndCache(cache, "ontologies", text);
    

    var doc = JSON.parse(text);
    var ontologies = doc;

    var ontologyDictionary = {};
    for (ontologyIndex in doc) {
        var ontology = doc[ontologyIndex];
      ontologyDictionary[ontology.acronym] = {"name":ontology.name, "uri":ontology["@id"]};
    }

    return sortOnKeys(ontologyDictionary);

}



/**
 * @description retrieves a list of ontologies from an external web source/portal and adds them to the panel as a restriction option
 * @param{Application} app
 * @param{FlowPanel} panel
 * @param{function} ontologiesRetrievalFnc - a method that return an object of ontologies sorted by key. The key is the acronym/identifier of the ontology.
 * @param{string} btnLabel - the label for the button
  * @param{string} service - the service where the info comes from
 */
function addOntologiesToPanel(app, panel, ontologiesRetrievalFnc, btnLabel, service_name) {
    panel.add(app.createTextBox().setName("columnName").setId("columnName").setTag("Column Name").setStyleAttribute("border", "thin solid #939598"));

    // get ontology names to populate the list box.
    var listBox = app.createListBox().setName("ontology").setId("ontology").setSize("150", "20");

    var ontologies = ontologiesRetrievalFnc();

    for (ontologyId in ontologies) {
        listBox.addItem(ontologyId + " - " + ontologies[ontologyId].name);
    }
    panel.add(listBox);


    var hidden_service = "hidden_service"
    panel.add(app.createHidden(hidden_service, service_name).setId("hidden_service"))

    var addRestrictionButton = app.createButton().setText(btnLabel).setStyleAttribute("background", "#81A32B").setStyleAttribute("font-family", "sans-serif").setStyleAttribute("font-size","11px").setStyleAttribute("color", "#ffffff").setStyleAttribute("border", "none");
    addRestrictionButton.setHeight(25).setWidth(150);
    addRestrictionButton.setId("ontologyRuleSet");


    var addRestrictionHandler = app.createServerClickHandler("addRestrictionHandler");
    addRestrictionHandler.addCallbackElement(app.getElementById("columnName"));
    addRestrictionHandler.addCallbackElement(app.getElementById("ontology"));
    addRestrictionHandler.addCallbackElement(app.getElementById("hidden_service"));


    addRestrictionButton.addClickHandler(addRestrictionHandler);

    panel.add(addRestrictionButton);
}


function showSettings() {
    var mydoc = SpreadsheetApp.getActiveSpreadsheet();

    var app = UiApp.createApplication().setHeight(580);

    var absolutePanel = app.createAbsolutePanel();
    absolutePanel.setSize(480, 560);


    absolutePanel.add(createLabel(app, "How do you want ontology terms to be entered in the spreadsheet?",
        "sans-serif", "bolder", "13px", "#000"), 15, 100);

    var useDefault = isCurrentSettingOnDefault();

    var placementStrategyOptions = app.createListBox().setName("strategy").setId("strategy").setSize("350", "27");
    placementStrategyOptions.addItem("Place hyperlinked term name in field");
    placementStrategyOptions.addItem("Place term name and accession in different fields");

  if(!useDefault) {
    placementStrategyOptions.setSelectedIndex(1);
  }

    absolutePanel.add(placementStrategyOptions, 15, 130);

    var option1Handler = app.createServerValueChangeHandler('setOntologyInsertionStrategy');
    option1Handler.addCallbackElement(absolutePanel);
    placementStrategyOptions.addChangeHandler(option1Handler);

    absolutePanel.add(createLabel(app, "Restrictions are used to limit the search space for specific columns in your Google Spreadsheet. Do you wish to restrict the search space for fields? ", "sans-serif", "bolder", "13px", "#000"), 15, 200);
    absolutePanel.add(createLabel(app, "All restrictions are added to the 'Restrictions' sheet.", "sans-serif", "lighter", "12px", "#000"), 15, 250);

    var header = app.createHorizontalPanel();

    header.add(createLabel(app, "Field Name",
        "sans-serif", "bolder", "12px", "#000").setSize("125", "20"));

    header.add(createLabel(app, "Ontology",
        "sans-serif", "bolder", "12px", "#000").setSize("82", "20"))

    absolutePanel.add(header, 15, 285);

    var flow = app.createFlowPanel();

    // BioPortal
    addOntologiesToPanel(app, flow, getBioPortalOntologies, "Add BioPortal Restriction", "BioPortal");
    absolutePanel.add(flow, 15, 290);

    // LOV
    addOntologiesToPanel(app, flow, getLinkedOpenVocabularies, "Add LOV Restriction", "LOV");
    absolutePanel.add(flow, 15, 300);

    // OLS
    addOntologiesToPanel(app, flow, getOLSOntologies, "Add OLS Restriction", "OLS");
    absolutePanel.add(flow, 15, 310);

    absolutePanel.add(app.createLabel().setId("status").setStyleAttribute("font-family", "sans-serif").setStyleAttribute("font-size", "12px"), 15, 420);

    var viewRestrictionsButton = app.createButton().setText("View All Restrictions").setStyleAttribute("background", "#666").setStyleAttribute("font-family", "sans-serif").setStyleAttribute("font-size","11px").setStyleAttribute("color", "#ffffff").setStyleAttribute("border", "none");;
    viewRestrictionsButton.setHeight(25).setWidth(140);

    var applyAndCloseButton = app.createButton().setText("Apply").setStyleAttribute("background", "#81A32B").setStyleAttribute("font-family", "sans-serif").setStyleAttribute("font-size","11px").setStyleAttribute("color", "#ffffff").setStyleAttribute("border", "none");;
    applyAndCloseButton.setHeight(25).setWidth(60);

    var viewRestrictionHandler = app.createServerClickHandler("viewRestrictionHandler");
    viewRestrictionsButton.addClickHandler(viewRestrictionHandler);

    var applyAndCloseHandler = app.createServerClickHandler("applyAndClose");
    applyAndCloseButton.addClickHandler(applyAndCloseHandler);

    absolutePanel.add(viewRestrictionsButton, 10, 445);
    absolutePanel.add(applyAndCloseButton, 400, 445);

    app.add(absolutePanel);

    createSettingsTab();
    mydoc.show(app);
}

function createSettingsTab() {
    var settingsSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Definições");
    if (settingsSheet == undefined) {
        var activeSheet = SpreadsheetApp.getActiveSheet();
        settingsSheet = SpreadsheetApp.getActiveSpreadsheet().insertSheet("Definições");
        settingsSheet.getRange("A1").setValue("insertTermInOneColumn");
        settingsSheet.getRange("B1").setValue(true);
        SpreadsheetApp.getActiveSpreadsheet().setActiveSheet(activeSheet);
    }
}

function viewRestrictionHandler(e) {
     var restrictionSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Restrições");

    if (restrictionSheet == undefined) {
        UiApp.getActiveApplication().getElementById("status").setText("Restriction sheet doesn't exist yet. Add a restriction and it will be created automatically.");
        return UiApp.getActiveApplication();
    } else {
        SpreadsheetApp.getActiveSpreadsheet().setActiveSheet(restrictionSheet);
    }
}

function isCurrentSettingOnDefault() {
    var settingsSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Definições");

    if (settingsSheet != undefined) {
        var settingsValue = settingsSheet.getRange("B1").getValue();
        if (settingsValue == false) {
            return false;
        }
    }
    return true;
}

function applyAndClose(e) {
  var app = UiApp.getActiveApplication();
  return app.close();
}

function addRestrictionHandler(field, ontologies) {
  var app, activeSheet, restrictionSheet, nextBlankRow, columnName, service;
  var ontology = "";
  try {
    restrictionSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Restrições");

    if (restrictionSheet == undefined) {
      activeSheet = SpreadsheetApp.getActiveSheet();
      restrictionSheet = SpreadsheetApp.getActiveSpreadsheet().insertSheet("Restrições");
      restrictionSheet.getRange("A1").setValue("Column Name");
      restrictionSheet.getRange("B1").setValue("Ontology");
      restrictionSheet.getRange("C1").setValue("Branch");
      restrictionSheet.getRange("D1").setValue("Version");
      restrictionSheet.getRange("E1").setValue("Ontology Name");
      //restrictionSheet.getRange("F1").setValue("Service");

      SpreadsheetApp.getActiveSpreadsheet().setActiveSheet(activeSheet);
    }

    app = UiApp.getActiveApplication();

    if (field === "") {
      app.getElementById("status").setText("Please enter a column name!");
    } else {
      nextBlankRow = findNextBlankRow(restrictionSheet);

      columnName = field
      if (Myindexof(ontologies,",") > -1) {
        ontologies = ontologies.split(", ");
        for (x in ontologies) {
          var size = parseFloat(x);
          if (size + 1 != ontologies.length) {
            ontology += ontologies[x] + ", ";
          } else {
            ontology += ontologies[x];
          }
        }
        restrictionSheet.getRange(nextBlankRow, 1).setValue(columnName);
        
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology);
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology);
      } else {
        ontology = ontologies;
        restrictionSheet.getRange(nextBlankRow, 1).setValue(columnName);
        
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology.substring(0, ontology.indexOf("-")));
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology.substring(ontology.indexOf("-")+1));
      }
      
      
      

      //service = e.parameter.hidden_service;

        restrictionSheet.getRange(nextBlankRow, 1).setValue(columnName);
        
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology);
        restrictionSheet.getRange(nextBlankRow, 2).setValue(ontology);
        
      app.getElementById("status").setText("Restriction for " + columnName + " added based on "+ ontology).setStyleAttribute("color", "#F00")
    }
    return app;
  }
  catch(err) {
    app = UiApp.getActiveApplication();
    Logger.log(err);
    app.getElementById("status").setText("Error caught: " + err);
    return err;
  }
}

function setOntologyInsertionStrategy(e) {
    var option = e.parameter.strategy;

    var settingsSheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName("Definições");
    settingsSheet.getRange("B1").setValue(option == "Place hyperlinked term name in field" ? true : false);
    return UiApp.getActiveApplication();
}


