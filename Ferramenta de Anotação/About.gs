function showAbout() {
    var mydoc = SpreadsheetApp.getActiveSpreadsheet();

    var app = UiApp.createApplication().setHeight(500);
  
    var absolutePanel = app.createAbsolutePanel();
    absolutePanel.setSize(480, 450);
    
  
    absolutePanel.add(app.createLabel("Esta ferramenta dá uso aos NCBO BioPortal Web Services, Linked Open Vocabularies services, AgroPortal Web Services and GeoNames Web Services para facilitar a anotação para ontologias a partir do Google Spreadsheets."));
    
  
    absolutePanel.add(app.createLabel("Esta ferramenta foi desenvolvida pelo o aluno Renato Correia como Projecto de Mestrado, no Instituto Superior Técnico, para ajudar os utilizadores a anotar os seus dados para ontologias duma forma mais fácil."));
                                            
    app.add(absolutePanel);
    mydoc.show(app);
}
