  $(document).ready(function(){
    var gameId = $('#systemInfo').data('gameid');
    $.getJSON('/report/getsystem?gameId=' + gameId, function(data){
      function SystemModel(){
        this.name = data.name
        this.align = data.planets.length > 1 ? "left":"center";
        this.Planets = data.planets; //_.sortBy(data.planets, function(p){return p.planet.radius * -1});
        _.map( this.Planets, function(planet){
          //--compat fixes
          if(planet.planet.radius <= 0 ) planet.planet.radius = 500;
          planet.starting_planet = planet.starting_planet || false; //ensure field presence
          planet.required_thrust_to_move = planet.required_thrust_to_move || 0; //ensure field presence
          // --end compat fixes
          if(planet.planet.temperature <= 33 && planet.planet.biome == 'earth') planet.planet.biome = 'ice'
          planet.imagePath = imageBaseUrl + 'planets/' + planet.planet.biome + '.png';
          planet.imageWidth = planet.planet.radius >= 1000 ? '200px' : Math.round(200 * planet.planet.radius / 1000) + 'px';
          planet.imageOffset = planet.planet.radius < 420 ? Math.round(200 * (420 - planet.planet.radius)/2000) + 'px': '0';
        });
      }
      ko.applyBindings( new SystemModel(), document.getElementById('systemInfo'))
    })
  })