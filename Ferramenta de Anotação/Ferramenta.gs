/**
 * Creates a menu entry in the Google Docs UI when the document is opened.
 */
function onOpen() {

  SpreadsheetApp.getUi().createAddonMenu()
      .addItem('Run Annotator...', 'showAnnotatorSidebar')
      .addSeparator()
      .addItem('Settings...', 'showSettings')
      .addItem('About', 'showAbout')
      .addToUi();
}


function showAnnotatorSidebar() {
  var ui = HtmlService.createHtmlOutputFromFile('Barra lateral Anotador')
      .setTitle('Anotador');
  SpreadsheetApp.getUi().showSidebar(ui);
}


function runAnnotator() {
  getRestrictionForTerm();
  return Anotator();
}
