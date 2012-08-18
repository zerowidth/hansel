$ ->
  grid = new Grid 46, 30 # 47 * 20 = 940, the container width
  grid.draw()

class Grid
  constructor: (@width, @height) ->
    @grid_size = 20
    @points = []
    for x in [0...@width]
      for y in [0...@height]
        @points.push [x,y,'open']
    d3.select('#grid-container')
      .append('svg:svg')
      .append('svg:g')
      .attr('transform', 'translate(1,1)')
      .attr('id', 'grid')

    # mousedown handled only on the grid
    $('body').mouseup =>
      @drag = false
      # console.log d3.select('#grid').selectAll('rect.closed').data().length

  draw: ->
    squares = d3.select('#grid').selectAll('rect').data(@points)

    squares.enter()
      .append('rect')
      .attr('x', (d, i) => d[0] * @grid_size)
      .attr('y', (d, i) => d[1] * @grid_size)
      .attr('width', @grid_size)
      .attr('height', @grid_size)
      .on('mousedown', @mousedown)
      .on('mouseover', @mouseover)
      .on('mouseout', @mouseout)

    squares.attr('class', (d, i) -> d[2])

  mousedown: (d, i) =>
    square = d3.select(d3.event.target)
    @drag = if square.classed('open') is true then 'open' else 'closed'
    @mouseover d, i

  mouseover: (d, i) =>
    square = d3.select(d3.event.target)
    if @drag
      if @drag is 'open' and square.classed('open')
        @close square
      else if @drag is 'closed' and square.classed('closed')
        @open square

  mouseout: (d, i) =>
    square = d3.select(d3.event.target)

  open: (selection) =>
    selection.attr('class', 'open')
    d = selection.datum()
    d[2] = 'open'
    selection.datum(d)

  close: (selection) =>
    selection.attr('class', 'closed')
    d = selection.datum()
    d[2] = 'closed'
    selection.datum(d)

