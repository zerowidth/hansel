$ ->
  grid = new Grid 46, 30 # 47 * 20 = 940, the container width
  grid.draw()

class Grid
  constructor: (@width, @height) ->
    @grid_size = 20
    @points = []
    for y in [0...@height]
      for x in [0...@width]
        @points.push [x,y,'open']
    @points[ @height + 1 ][2] = 'start'
    @points[ (@height * (@width - 2)) + (@width - 2) ][2] = 'dest'
    d3.select('#grid-container')
      .append('svg:svg')
      .append('svg:g')
      .attr('transform', 'translate(1,1)')
      .attr('id', 'grid')

    # mousedown handled only on the grid
    $('body').mouseup @mouseup

  grid: -> d3.select('#grid')

  draw: ->
    squares = @grid().selectAll('rect').data(@points)

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
    @drag = square.attr 'class'
    @mouseover d, i

  mouseup: =>
    # replace all other classes with 'start' or 'dest'
    if @drag is 'start'
      @grid().selectAll('rect.start').attr('class', 'start')
    else if @drag is 'dest'
      @grid().selectAll('rect.dest').attr('class', 'dest')

    @drag = false
    # console.log d3.select('#grid').selectAll('rect.closed').data().length

  mouseover: (d, i) =>
    square = d3.select(d3.event.target)
    switch @drag
      when 'open'
        if square.classed('open')
          @close square
      when 'closed'
        if square.classed('closed')
          @open square
      when 'start'
        if not square.classed('dest')
          before = @grid().selectAll('rect.start')
          before.classed('start', false)
          if before.attr('class') is ""
            before.attr('class', 'open')
          square.classed('start', true)
      when 'dest'
        if not square.classed('start')
          before = @grid().selectAll('rect.dest')
          before.classed('dest', false)
          if before.attr('class') is ""
            before.attr('class', 'open')
          square.classed('dest', true)

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

