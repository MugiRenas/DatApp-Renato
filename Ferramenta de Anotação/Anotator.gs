function Anotator() {
    try {
      // only perform a search if there is a difference
      var sheet = SpreadsheetApp.getActiveSheet();
      var dataRange = sheet.getActiveRange();
      var data = dataRange.getValues();
      var z;
      var j = 2;
      var array;
      var array2;
      var definition;
      var definition2 = [];
      var aux = [];
      var y = 0;
        
      var row = data[0];
      if (row[4] != "") {
        var restriction = findRestrictionForCurrentColumn(row[0]);
        var term = row[1];
        /* } else {
        var term = row[2];
        }*/
        if (term.length > 2) {
          // todo check if particular column has a restriction in the hidden sheet. If so, restrict the search.
          
          
          
          
          //Logger.log("restriction.service==>"+restriction.service);
          if (restriction) {
            if (Myindexof(restriction.ontologyId,",") > -1) {
              aux = restriction.ontologyId.split(", ");
              while (y < aux.length) {
                if (aux[y] == "geonames.org" || aux[y] == "GAZ") {
                  var searchString3 = "http://api.geonames.org/searchJSON?q=" + term + "&username=renato_correia";
                  z = 3;
                }
                else {
                  z = 1;
                }
              }
            }
            if (restriction.ontologyId == "geonames.org" || restriction.ontologyId == "GAZ") {
              var searchString3 = "http://api.geonames.org/searchJSON?q=" + term + "&username=renato_correia";
              z = 3;
            }
            else {
              z = 1;
            }
          }
          
          if (z == 1) {
            
            
            
            var text, key;
            var o = encodeURIComponent(restriction.ontologyId).replace(/%0A/g,'');
            /*if (cacheResult != undefined) {
            return JSON.parse(cacheResult);
            // SpreadsheetApp.getActiveSpreadsheet().toast("Terms retrieved from cache.", "Cache accessed", -1);
            } else {*/
            text = UrlFetchApp.fetch("tc.students.precisemed.org/RenatoCorreiaAPI/rest/json;param1=" + term + ";param2=" + o).getContentText();
            
            if (text != "[]") {
              var doc = JSON.parse(text);
              var key2 = doc[0].name;
              var uri = doc[0].uri;
              /*var final = searchResultBeans[0];
              
              for (var resultIndex in searchResultBeans) {
              var result = searchResultBeans[resultIndex];
              var ontologyLabel = result.links.ontology.substring(result.links.ontology.lastIndexOf("/") + 1);
              
              if (ontologyDictionary[ontologyLabel] == undefined) {
              ontologyDictionary[ontologyLabel] = {"ontology-name": ontologies[ontologyLabel].name, "terms": []};
              }
              
              //key for the cache
              key = ontologyLabel +":" + result["@id"]
              
              var ontology_record = {
              "label": result.prefLabel,
              "id": key,
              "ontology-label": ontologyLabel,
              "ontology-name": ontologies[ontologyLabel].name,
              "accession": result["@id"], //result.links.self,
              "ontology": result.links.ontology,
              "url": result["@id"]//result.links.ui                     
              };
              var ontology_record_string = JSON.stringify(ontology_record);
              Logger.log("BioPortal ontology_record " + ontology_record_string)
              
              storeInCache(key, ontology_record_string);
              ontologyDictionary[ontologyLabel].terms.push(ontology_record);
              }
              
              ontologyDictionary.sortedOntologies = sortDictAndReturnSortedKeys(ontologyDictionary);
              storeInCache(searchString, JSON.stringify(ontologyDictionary));
              
              var key2 = ontologyLabel +":" + final["@id"]
              
              if (final.definition != null) {
              var definition = final.definition;
              } else {
              definition = term;
              }*/
              handleTermInsertion(uri, sheet.getRange(sheet.getActiveCell().getRow(), 3), key2);
              
              j++;
              
              //return ontologyDictionary;
            } else {
              j++;
            }
          }
          
          
          // we cache results and try to retrieve them on every new execution.
          if (z == 3) {
            var text, key;
            text = UrlFetchApp.fetch(searchString3).getContentText();
            var doc = JSON.parse(text);
            //var doc = JSON.parse(text);
            var searchResultBeans = doc.geonames;
            var ontologyDictionary = {};
            var final = searchResultBeans[0];
            var result = searchResultBeans[0];
            //var ontologyLabel = result.links.ontology.substring(result.links.ontology.lastIndexOf("/") + 1);
            
            /*if (ontologyDictionary[ontologyLabel] == undefined) {
            ontologyDictionary[ontologyLabel] = {"ontology-name": ontologies2[ontologyLabel].name, "terms": []};
            }*/
            
            //key for the cache
            //key = ontologyLabel +":" + result["@id"]
            var term_id = "http://www.geonames.org/" + result.geonameId + "/";
            
            
            
            definition = "Location";
            
            handleTermInsertionGeo(term_id, sheet.getRange(j, 3), definition);
            
            
            j++;
          } 
        } else {
          throw '';
        }
      }
    }
    
      catch (e) {
        throw e;
      }
      
      return {};
    
}


function handleTermInsertion(term_id, position, definition) {
  Logger.log("handleTermInsertion - here we are - term is: " + term_id);
  try {
    var sheet = SpreadsheetApp.getActiveSheet();
    var selectedRange = position;
    Logger.log("handleTermInsertion - term_id:" + term_id);
    
    var sourceAndAccessionPositions = getSourceAndAccessionPositionsForTerm(selectedRange.getColumn());

    if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
        insertOntologySourceInformationInInvestigationBlock(ontologyObject);
    }

    for (var row = selectedRange.getRow(); row <= selectedRange.getLastRow(); row++) {

        // if the currently selected column is an ISA defined ontology term, then we should insert the source and accession in subsequent
        // columns and add the ontology source information to the investigation file if it doesn't already exist.
        if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
            sheet.getRange(row, selectedRange.getColumn()).setValue(ontologyObject.term);
            sheet.getRange(row, sourceAndAccessionPositions.sourceRef).setValue(ontologyObject.ontologyId);
            sheet.getRange(row, sourceAndAccessionPositions.accession).setValue(ontologyObject.url);
        } else {

            var isDefaultInsertionMechanism = isCurrentSettingOnDefault();
            var selectedColumn = selectedRange.getColumn();
            var nextColumn = selectedColumn + 1;
            Logger.log("handleTermInsertion - ready to insert ontology object, with default mechanism set to " + isDefaultInsertionMechanism);
            if (!isDefaultInsertionMechanism) {
                //sheet.getRange(row, selectedColumn).setValue(ontologyObject.term);
                sheet.getRange(row, nextColumn).setValue(term_id);
            } else {
                sheet.getRange(row, selectedColumn).setValue(term_id);
                sheet.getRange(row, nextColumn).setValue(definition);
            }
        }
    }
    insertTermInformationInTermSheet(term_id);
  }
  catch(err) {
    Logger.log(err);
    throw err;
  }

}

function handleTermInsertionGeo(term_id, position, definition) {
  Logger.log("handleTermInsertion - here we are - term is: " + term_id);
  try {
    var sheet = SpreadsheetApp.getActiveSheet();
    var selectedRange = position;
    Logger.log("handleTermInsertion - term_id:" + term_id);
    
    var sourceAndAccessionPositions = getSourceAndAccessionPositionsForTerm(selectedRange.getColumn());
    // add all terms into a separate sheet with all their information.

    Logger.log("handleTermInsertion - sourceAndAccessionPositions:");
    Logger.log(sourceAndAccessionPositions);

    /*if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
        insertOntologySourceInformationInInvestigationBlock(ontologyObject);
    }*/

    for (var row = selectedRange.getRow(); row <= selectedRange.getLastRow(); row++) {

        // if the currently selected column is an ISA defined ontology term, then we should insert the source and accession in subsequent
        // columns and add the ontology source information to the investigation file if it doesn't already exist.
        if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
            sheet.getRange(row, selectedRange.getColumn()).setValue(term);
            sheet.getRange(row, sourceAndAccessionPositions.sourceRef).setValue("geonames.org");
            sheet.getRange(row, sourceAndAccessionPositions.accession).setValue(term_id);
        } else {

            var isDefaultInsertionMechanism = isCurrentSettingOnDefault();
            var selectedColumn = selectedRange.getColumn();
            var nextColumn = selectedColumn + 1;
            Logger.log("handleTermInsertion - ready to insert ontology object, with default mechanism set to " + isDefaultInsertionMechanism);
            if (!isDefaultInsertionMechanism) {
                sheet.getRange(row, selectedColumn).setValue(term_id);
                //sheet.getRange(row, nextColumn).setValue(term_id);
            } else {
                sheet.getRange(row, selectedColumn).setValue(term_id);
                sheet.getRange(row, nextColumn).setValue(definition);
            }
        }
    }
    //insertTermInformationInTermSheet(ontologyObject);
  }
  catch(err) {
    Logger.log(err);
    throw err;
  }

}

function Myindexof(s,text)
{
  var lengths = s.length;
  var lengtht = text.length;
  for (var i = 0;i < lengths - lengtht + 1;i++)
  {
    if (s.substring(i,lengtht + i) == text)
      return i;
  }
  return -1;
}

function handleTermInsertionMultiple(term_id, position, definition2) {
  Logger.log("handleTermInsertion - here we are - term is: " + term_id);
  try {
    var sheet = SpreadsheetApp.getActiveSheet();
    var selectedRange = position;
    var url = [];
    var term = [];
    var definition3 = [];
    for (x in term_id) {
      var size = parseFloat(x);
      Logger.log("handleTermInsertion - term_id:" + term_id);
      var textTerm = fetchFromCache(term_id[x]);
      Logger.log("handleTermInsertion - from cache textTerm:" + textTerm);
      term[x] = JSON.parse(textTerm);
      Logger.log("handleTermInsertion - term:" + textTerm);
      Logger.log(term);
      var ontologyObject = {
        "term": term[x]["label"],
        "accession": term_id[x],
        "ontologyId": term[x]["ontology-label"],
        "ontologyVersion": term[x]["ontology"],
        "ontologyDescription": term[x]["ontology-name"],
        "url": term[x]["url"]
      }
      insertTermInformationInTermSheet(ontologyObject);
      if (size + 1 != term_id.length) {
        url += term[x]["url"] + ", ";
        definition3 += definition2[x] + ", ";
      } else {
        url += term[x]["url"];
        definition3 += definition2[x];
      }
    }
      Logger.log("handleTermInsertion - ontologyObject:"+ JSON.stringify(ontologyObject));
      // figure out whether the Term Source REF and Term Accession Number columns exist, if they do exist at all. Insertion technique will vary
      // depending on the file being looked at.
      var sourceAndAccessionPositions = getSourceAndAccessionPositionsForTerm(selectedRange.getColumn());
      // add all terms into a separate sheet with all their information.
      
      Logger.log("handleTermInsertion - sourceAndAccessionPositions:");
      Logger.log(sourceAndAccessionPositions);
      
      if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
        insertOntologySourceInformationInInvestigationBlock(ontologyObject);
      }
      
      for (var row = selectedRange.getRow(); row <= selectedRange.getLastRow(); row++) {
        
        // if the currently selected column is an ISA defined ontology term, then we should insert the source and accession in subsequent
        // columns and add the ontology source information to the investigation file if it doesn't already exist.
        if (sourceAndAccessionPositions.sourceRef != undefined && sourceAndAccessionPositions.accession != undefined) {
          sheet.getRange(row, selectedRange.getColumn()).setValue(ontologyObject.term);
          sheet.getRange(row, sourceAndAccessionPositions.sourceRef).setValue(ontologyObject.ontologyId);
          sheet.getRange(row, sourceAndAccessionPositions.accession).setValue(ontologyObject.url);
        } else {
          
          var isDefaultInsertionMechanism = isCurrentSettingOnDefault();
          var selectedColumn = selectedRange.getColumn();
          var nextColumn = selectedColumn + 1;
          Logger.log("handleTermInsertion - ready to insert ontology object, with default mechanism set to " + isDefaultInsertionMechanism);
          if (!isDefaultInsertionMechanism) {
            sheet.getRange(row, nextColumn).setValue(ontologyObject.url);
          } else {
            sheet.getRange(row, selectedColumn).setValue(url);
            sheet.getRange(row, nextColumn).setValue(definition3);
          }
        }
      }
      insertTermInformationInTermSheet(ontologyObject);
  }
  catch(err) {
    Logger.log(err);
    throw err;
  }

}

